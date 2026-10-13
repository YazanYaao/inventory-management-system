.PHONY: help build package test run clean docker-build docker-up docker-down docker-logs

# Auto-detect whether ims directory is present
ifeq ($(wildcard ims/pom.xml),ims/pom.xml)
  MVNW := cd ims && ./mvnw
  COMPOSE := cd ims && docker compose
else
  MVNW := ./mvnw
  COMPOSE := docker compose
endif

help:
	@echo "============================================================"
	@echo "  IMS PRO - Makefile Command Reference"
	@echo "============================================================"
	@echo "  make build         - Compile & package app (skip tests)"
	@echo "  make test          - Run full 15-test automated test suite"
	@echo "  make package       - Run tests and package JAR"
	@echo "  make run           - Run Spring Boot app locally"
	@echo "  make clean         - Clean build target directory"
	@echo "  make docker-build  - Build Docker containers"
	@echo "  make docker-up     - Start Docker containers in background"
	@echo "  make docker-down   - Stop and tear down Docker containers"
	@echo "  make docker-logs   - Follow live container logs"
	@echo "============================================================"

build:
	$(MVNW) clean package -DskipTests

package:
	$(MVNW) clean package

test:
	$(MVNW) test

run:
	$(MVNW) spring-boot:run

clean:
	$(MVNW) clean

docker-build:
	$(COMPOSE) build

docker-up:
	$(COMPOSE) up -d

docker-down:
	$(COMPOSE) down

docker-logs:
	$(COMPOSE) logs -f
