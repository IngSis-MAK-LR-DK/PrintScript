plugins {
    id("printscript.java-conventions")
    application
}

dependencies {
    implementation(project(":common"))
    implementation(project(":lexer"))
    implementation(project(":parser"))
    implementation(project(":interpreter"))
    implementation(project(":formatter"))
    implementation(project(":analyzer"))

    // Operator plugins: discovered at runtime via ServiceLoader, never a compile-time dependency.
    runtimeOnly(project(":plugins:modulo-operator"))
}

application {
    mainClass.set("edu.austral.ingsis.printscript.cli.Main")
}
