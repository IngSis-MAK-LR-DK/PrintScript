# PrintScript

Intérprete, formateador y analizador estático para PrintScript, un subconjunto de TypeScript orientado a imprimir texto y números. Todo se usa desde una CLI.

## Requisitos y build

Hace falta JDK 17 o una versión que soporte Gradle 8.10. Con versiones más nuevas, por ejemplo Java 26, `./gradlew` falla: apuntá `JAVA_HOME` a un JDK 17.

```bash
./gradlew check               # compila, verifica formato y estilo, corre tests y exige 80% de cobertura
./gradlew spotlessApply       # corrige el formato
./gradlew :cli:installDist    # genera el ejecutable en cli/build/install/cli/bin
```

El hook de pre-commit corre `./gradlew check` antes de cada commit. Se instala una vez por clon:

```bash
pip install pre-commit
pre-commit install
```

## Uso

```text
printscript <validation|execution|formatting|analyzing> <archivo> [--version 1.0|1.1] [--config <ruta>]
```

| Operación | Qué hace |
|---|---|
| `validation` | Revisa sintaxis y semántica sin ejecutar el programa. |
| `execution` | Ejecuta el programa. |
| `formatting` | Escribe el programa formateado por la salida estándar. |
| `analyzing` | Informa las violaciones de reglas con su posición. Termina con código 1 si encuentra alguna. |

La versión por defecto es 1.0. El progreso del parsing y los errores salen por `stderr`, y el resultado por `stdout`. Los errores indican fila y columna de inicio y de fin.

### Configuración

`--config` acepta un archivo `.yaml`, `.yml` o `.json`. Los campos que no estén en el archivo toman el valor por defecto.

| Formatter | Por defecto | Efecto |
|---|---|---|
| `spaceBeforeColon` | `false` | Espacio antes del `:` en una declaración. |
| `spaceAfterColon` | `true` | Espacio después del `:` en una declaración. |
| `spaceAroundEquals` | `true` | Espacio antes y después del `=`. |
| `newLinesBeforePrintln` | `0` | Líneas en blanco (0, 1 o 2) entre dos `println` seguidos. Es el comportamiento que exige el TCK. |
| `indentSize` | `2` | Indentación dentro de `if`/`else`. |
| `spaceAroundParens` | `false` | Formato `println ( x );`. |
| `ifBraceBelowLine` | `false` | Pone la `{` de `if`/`else` en su propia línea. |

| Analyzer | Por defecto | Efecto |
|---|---|---|
| `identifierCaseCheckEnabled` | `true` | Activa la regla de nombres. |
| `identifierCase` | `CAMEL_CASE` | `CAMEL_CASE` o `SNAKE_CASE`. |
| `printlnArgumentMustBeIdentifierOrLiteral` | `true` | `println` solo acepta un identificador o un literal. |
| `readArgumentMustBeIdentifierOrLiteral` | `true` | Lo mismo para `readInput` y `readEnv`. |

## Arquitectura

El [diagrama de componentes](docs/diagrams/component-diagram.puml) muestra los módulos y sus dependencias. También están el [diagrama de clases](docs/diagrams/class-diagram.puml) y el [de secuencia](docs/diagrams/sequence-diagram.puml).

| Módulo | Responsabilidad | Depende de |
|---|---|---|
| `common` | Tokens, posiciones, excepciones, versiones y operadores. | Ninguno |
| `ast` | Nodos del árbol y sus visitors. | `common` |
| `config` | Contrato para cargar configuraciones YAML o JSON. | Ninguno |
| `lexer` | Convierte caracteres en tokens de forma perezosa. | `common` |
| `parser` | Convierte tokens en sentencias, una por vez, y controla qué admite cada versión. | `common`, `ast` |
| `interpreter` | Ejecuta sentencias y hace la validación semántica estática. | `common`, `ast` |
| `formatter` | Regenera el código según las reglas configuradas. | `ast`, `config` |
| `analyzer` | Aplica las reglas de análisis estático. | `ast`, `config` |
| `cli` | Interpreta los argumentos y conecta los módulos. | Todos los anteriores |
| `plugins:modulo-operator` | Agrega el operador `%` como ejemplo de extensión. | `common` |

### Cómo interactúan

Los módulos forman un flujo perezoso, de modo que un archivo más grande que la memoria se procesa de a una sentencia:

```text
archivo -> lexer -> TokenStream -> parser -> Iterator<Statement> -> interpreter | formatter | analyzer
```

- `lexer` y `parser` no se conocen entre sí: se conectan por `TokenStream`, que está en `common`.
- `cli` es el único módulo que crea las implementaciones concretas.
- Los operadores nuevos se agregan sin tocar el núcleo: un módulo implementa `OperatorDefinition` y se registra para `ServiceLoader`. `cli` lo descubre en tiempo de ejecución.
- La salida del formatter y los hallazgos del analyzer se entregan a medida que aparecen, sin acumularlos.
