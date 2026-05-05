#------------------------------------------------------------------------
# 🦭 Makefile para Podman - Proyecto POS Backend
#------------------------------------------------------------------------

#------------------------------------------------------------------------
# 📦 Variables de entorno y configuración
#------------------------------------------------------------------------
CURDIR := $(abspath $(dir $(lastword $(MAKEFILE_LIST))))
ROOT_DIR := $(CURDIR)
GRADLEW_CMD := ./gradlew
BUILD_DIR := $(CURDIR)/.build

# Usaremos podman-compose.yml
COMPOSE_FILE := podman-compose.yml
COMPOSE_PROJECT_NAME := pos # Cadena literal fija
DB_CONTAINER_NAME_FULL := pos-db # Cadena literal fija (se usará para otros comandos, pero no para el healthcheck de DB)

# ✅ SOLUCIÓN PARA MAC: Detectar el socket de Podman automáticamente
export DOCKER_HOST ?= unix://$(shell podman machine inspect --format '{{.ConnectionInfo.PodmanSocket.Path}}')

# Archivos de entorno
ENV_FILE := $(abspath $(CURDIR)/.env)

COMPOSE_PROVIDER ?= podman
export DOCKER_BUILDKIT := 0
export COMPOSE_DOCKER_CLI_BUILD ?= 0

# --- Carga de variables de entorno para Make ---
ifneq ("$(wildcard $(ENV_FILE))","")
    include $(ENV_FILE)
    export $(shell sed 's/=.*//' $(ENV_FILE))
endif

# Puertos de servicios (con valores por defecto si no se cargaron del env)
APP_PORT_HOST ?= 9090
DB_PORT_HOST ?= 5432 # Aseguramos que este sea el puerto interno de la BD
DB_NAME ?= pos_db
DB_USER ?= admin

DOCKER_USERNAME ?=
DOCKER_PASSWORD ?=


# Comandos base
RUN := COMPOSE_PROVIDER=$(COMPOSE_PROVIDER) COMPOSE_PROJECT_NAME=$(COMPOSE_PROJECT_NAME)
COMPOSE_BASE := $(RUN) podman compose -f $(COMPOSE_FILE)

# Comandos específicos por entorno
COMPOSE_LOCAL := $(COMPOSE_BASE) --env-file $(ENV_FILE)

# Variables de utilidad para salida limpia
Q := @
ifndef VERBOSE
.SILENT:
endif

# Funciones de health check adaptadas
define check_health_url
	@count=0; \
	echo "   🔍 Verificando salud en $(1)..."; \
	HEALTH_CURL_CMD="curl -v $(1)"; \
	until $${HEALTH_CURL_CMD}; [ $$? -eq 0 ]; do \
		count=$$((count + 1)); \
		if [ $$count -ge 30 ]; then \
			echo "   ❌ ERROR: $(2) nunca arrancó en la URL $(1)"; \
			exit 1; \
		fi; \
		echo "   ⏳ $(2) está iniciando... (intento $$count/30)"; \
		sleep 5; \
		done; \
		echo "   ✅ $(2) está UP y respondiendo!"
endef

define print_row
	NAME=$$(echo $(1) | cut -d':' -f1); \
	PORT=$$(echo $(1) | cut -d':' -f2); \
	CONTAINER_FULL_NAME=$(COMPOSE_PROJECT_NAME)-$${NAME}; \
	RAW_STATUS=$$(podman inspect -f '{{.State.Status}}' $$CONTAINER_FULL_NAME 2>/dev/null || echo "down"); \
	RAW_HEALTH=$$(podman inspect -f '{{.State.Health.Status}}' $$CONTAINER_FULL_NAME 2>/dev/null || echo "n/a"); \
	STATUS=$$(echo $$RAW_STATUS | tr '[:lower:]' '[:upper:]'); \
	HEALTH_VAL=$$(echo $$RAW_HEALTH | tr '[:lower:]' '[:upper:]'); \
	if [ "$$HEALTH_VAL" = "HEALTHY" ]; then \
		HEALTH="\033[0;32m$$HEALTH_VAL\033[0m"; \
	else \
		HEALTH="\033[0;31m$$HEALTH_VAL\033[0m"; \
	fi; \
	printf "| %-20s | %-12s | %-10s | %-24b |\n" "$$NAME" "$$STATUS" "$$PORT" "$$HEALTH"
endef


#🎯 COMANDOS PRINCIPALES
#------------------------------------------------------------------------
.PHONY: help
help: ## 📚 Muestra esta ayuda
	@echo "Uso: make <comando>"
	@echo ""
	@echo "Comandos principales:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'
	@echo ""

start: up ## 🚀 Iniciar servicios
stop: down ## 🛑 Detener servicios
restart: stop start ## 🔄 Reiniciar servicios

#🏗️ CONSTRUCCIÓN
build-jars: fix-line-endings ## 🔨 Construir JAR de la aplicación
	@echo "🔨 Construyendo JAR de la aplicación..."
	$(Q) $(GRADLEW_CMD) bootJar
	@echo "✅ JAR generado exitosamente."

build-images: podman-auth build-jars ## 🏗️ Construir imágenes de la aplicación y DB
	@echo "======================================================================"
	@echo "🌍 AMBIENTE: Desarrollo"
	@echo "======================================================================"
	$(COMPOSE_LOCAL) build

#🔐 AUTENTICACIÓN
podman-auth: ## 🔑 Autenticar y asegurar imagen base
	@mkdir -p $(BUILD_DIR) # Aseguramos que BUILD_DIR exista para el authfile
	@echo "==========================================================================================================================================================================="
	@echo "🔑 Configurando autenticación automática para Podman..."
	@echo "==========================================================================================================================================================================="
	-@podman logout docker.io > /dev/null 2>&1
	@if [ -n "$(DOCKER_USERNAME)" ] && [ -n "$(DOCKER_PASSWORD)" ]; then \
		echo "Iniciando sesión en docker.io como $(DOCKER_USERNAME)..."; \
		echo "$(DOCKER_PASSWORD)" | podman login docker.io -u "$(DOCKER_USERNAME)" --password-stdin --authfile $(BUILD_DIR)/config.json || echo "⚠️ Login fallido (continuando sin login)..."; \
	else \
		echo "⚠️ No se encontraron credenciales en .env. Saltando login."; \
	fi
	@echo "📥 Asegurando imagen base Java 21..."
	@if [ -f "$(BUILD_DIR)/config.json" ]; then \
		podman pull eclipse-temurin:21-jdk-alpine --authfile $(BUILD_DIR)/config.json || echo "⚠️ Error descarga imagen base (puede que ya exista)"; \
	else \
		podman pull eclipse-temurin:21-jdk-alpine || echo "⚠️ Error descarga imagen base (puede que ya exista)"; \
	fi
	@echo "✅ Imagen base Java 21 lista."

#🚀 DESPLIEGUE
up: podman-ready build-images ## 🚀 Levantar servicios (DB y App)
	@echo "======================================================================================"
	@echo "🚀 INICIANDO DESPLIEGUE"
	@echo "======================================================================================"

	@echo "🏗️  Levantando servicios..."
	$(COMPOSE_LOCAL) up -d

	@echo "⏳ Esperando 30s para que los servicios se estabilicen..."
	@sleep 30

	@$(MAKE) health

	@echo ""
	@echo "========================================================================"
	@echo "                RESUMEN FINAL DE DESPLIEGUE"
	@printf "| %-20s | %-12s | %-10s | %-24b |\n" "SERVICIO" "ESTADO" "PUERTO" "HEALTH"

	@echo "--------------------------- BASES DE DATOS -----------------------------"
	@$(call print_row,db:$(DB_PORT_HOST));

	@echo "-------------------------- APLICACIÓN ----------------------------------"
	@$(call print_row,app:$(APP_PORT_HOST));

	@echo "========================================================================"
	@echo "✅ Proceso completado exitosamente."
	@echo "La aplicación debería estar disponible en http://localhost:$(APP_PORT_HOST)"


#🧹 LIMPIEZA
down: podman-ready ## 🛑 Detener y eliminar servicios
	@echo "======================================================================"
	@echo "🧹 DETENIENDO Y ELIMINANDO SERVICIOS"
	$(COMPOSE_LOCAL) down -v --remove-orphans
	@echo "✅ Servicios detenidos y eliminados."

clean-gradle: ## 🧹 Limpiar caché de Gradle
	$(Q) $(GRADLEW_CMD) clean

clean-podman-prune: podman-ready ## 🧹 Limpiar recursos de Podman
	$(Q) rm -rf $(BUILD_DIR) 2>/dev/null || true
	$(Q) podman system prune -a -f --volumes

fix-line-endings:
	@echo "🛠️ Corrigiendo terminaciones de línea..."
	$(Q) dos2unix $(GRADLEW_CMD) 2>/dev/null || true
	$(Q) chmod +x $(GRADLEW_CMD) 2>/dev/null || true # Asegura que gradlew sea ejecutable
	$(Q) dos2unix $(MAKEFILE_LIST) 2>/dev/null || true # Limpiar el propio Makefile

podman-ready:
	@echo "⚙️ Verificando Podman..."
	@podman machine ls >/dev/null 2>&1 || { echo "🚨 ERROR: Podman machine no responde."; exit 1; }

health: podman-ready ## 💚 Verificación de salud completa
	@echo "DEBUG: COMPOSE_PROJECT_NAME is '$(COMPOSE_PROJECT_NAME)'"
	@echo "DEBUG: DB_CONTAINER_NAME_FULL is '$(DB_CONTAINER_NAME_FULL)'"
	@echo "DEBUG: DB_USER is '$(DB_USER)'"
	@echo "DEBUG: DB_NAME is '$(DB_NAME)'"
	@echo "🩺 Verificación de salud..."
	@echo "🔍 Base de datos (pg_isready):"
	# CORREGIDO: Hardcodeamos el nombre del contenedor para evitar el problema del espacio
	@DB_HEALTH_CMD="podman exec pos-db pg_isready -U \"$(DB_USER)\" -d \"$(DB_NAME)\""; \
	echo "DEBUG: Ejecutando comando de salud de DB: $$DB_HEALTH_CMD"; \
	bash -c "$$DB_HEALTH_CMD" && echo "   ✅ Base de Datos" || echo "   ❌ Base de Datos"

	@echo "🔍 Aplicación (Health Actuator):"
	$(call check_health_url,http://localhost:$(APP_PORT_HOST)/actuator/health,Aplicación POS)

status: podman-ready ## 📊 Estado de todos los servicios
	$(COMPOSE_LOCAL) ps

fix-container-orphans: ## 🧹 Limpiar contenedores huérfanos (Forzado)
	@echo "🧹 Limpiando contenedores huérfanos (Forzado)..."
	# 1. Intenta down normal del proyecto
	$(COMPOSE_LOCAL) down -v --remove-orphans 2>/dev/null || true
	# 2. **Elimina forzadamente** contenedores que empiecen con el nombre del proyecto
	$(Q) podman rm -f $$(podman ps -a --format '{{.Names}}' | grep '^$(COMPOSE_PROJECT_NAME)_') 2>/dev/null || true
	# 3. Elimina contenedores detenidos globalmente (por si el paso 2 falla)
	$(Q) podman rm -f $$(podman ps -aq --filter status=exited) 2>/dev/null || true
	# 4. Purga el sistema
	$(Q) podman system prune -f 2>/dev/null || true