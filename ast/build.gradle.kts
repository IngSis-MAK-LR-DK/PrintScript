plugins {
    id("printscript.java-conventions")
}

dependencies {
    // Statement/Expression exponen Position (y ExtendedBinaryExpression, OperatorDefinition)
    // en su API publica, asi que esto tiene que ser "api", no "implementation".
    api(project(":common"))
}
