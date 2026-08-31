plugins {
    id("printscript.java-conventions")
    id("printscript.published-library")
}

dependencies {
    api(project(":common"))
    api(project(":ast"))
}
