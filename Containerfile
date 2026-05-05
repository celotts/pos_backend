# Use an official PostgreSQL image as a base
FROM postgres:16-alpine

# Set environment variables for PostgreSQL
# These will be used to create the initial database and user
ENV POSTGRES_DB=pos_db
ENV POSTGRES_USER=pos_user
ENV POSTGRES_PASSWORD=pos_password

# Copy the initialization script
COPY db/init/init.sql /docker-entrypoint-initdb.d/

# Expose the default PostgreSQL port
EXPOSE 5432

# The default command of the postgres image starts the PostgreSQL server
# No need to specify CMD here unless you want to override it
