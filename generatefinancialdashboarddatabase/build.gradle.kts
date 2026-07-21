plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set(
        "com.example.generatefinancialdashboarddatabase.FinancialDashboardDataInstantiationKt"
    )
}

dependencies {
    implementation(project(":financialmodels"))
    implementation("org.xerial:sqlite-jdbc:3.50.3.0")
}