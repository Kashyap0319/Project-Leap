rootProject.name = "api-monitoring-platform"

include(":backend:collector-service", ":backend:kotlin-tracker", ":backend:shared-contracts")

project(":backend:collector-service").projectDir = file("backend/collector-service")
project(":backend:kotlin-tracker").projectDir = file("backend/kotlin-tracker")
project(":backend:shared-contracts").projectDir = file("backend/shared-contracts")
