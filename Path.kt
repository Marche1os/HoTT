
/**
 * Пример 1: транспорт
 */
class Equiv<A, B>(val to: (A) -> B, val from: (B) -> A) {

    // эквивалентность: round-trip должен возвращать исходное значение.
    fun isWitnessedAtA(a: A): Boolean = from(to(a)) == a
    fun isWitnessedAtB(b: B): Boolean = to(from(b)) == b

    // Транспорт: берём свойство/функцию, определённую на A,
    // и получаем ту же функцию, но работающую на B
    fun <R> transport(propertyOnA: (A) -> R): (B) -> R =
        { b -> propertyOnA(from(b)) }
}

data class Fahrenheit(val value: Double)

fun example1Univalence() {
    println("Equiv и настоящий transport ===")

    val celsiusFahrenheit = Equiv<Double, Fahrenheit>(
        to = { c -> Fahrenheit(c * 9.0 / 5.0 + 32.0) },
        from = { f -> (f.value - 32.0) * 5.0 / 9.0 }
    )

    // Сначала предъявляем доказательство, что это эквивалентность,
    // а не пара случайных функций:
    check(celsiusFahrenheit.isWitnessedAtA(0.0))
    check(celsiusFahrenheit.isWitnessedAtA(100.0))
    check(celsiusFahrenheit.isWitnessedAtB(Fahrenheit(32.0)))
    println("Round-trip подтверждён: C -> F -> C и F -> C -> F возвращают исходное значение")

    // Свойство определено только для градусов Цельсия:
    fun isFreezingCelsius(c: Double): Boolean = c <= 0.0

    // теперь переносим его на градусы фаренгейта только за счёт структуры эквивалентности:
    val isFreezingFahrenheit = celsiusFahrenheit.transport(::isFreezingCelsius)

    println("isFreezingFahrenheit(32°F) = ${isFreezingFahrenheit(Fahrenheit(32.0))}")
    println("isFreezingFahrenheit(50°F) = ${isFreezingFahrenheit(Fahrenheit(50.0))}")
    check(isFreezingFahrenheit(Fahrenheit(32.0)))
    check(!isFreezingFahrenheit(Fahrenheit(50.0)))
}

/**
 * путь как вычисление
 */
class Path<T>(val start: T, val label: String, val proofFn: (T) -> T) {
    val end: T = proofFn(start)
    override fun toString(): String = "$start --[$label]--> $end"
}

fun <T> Path<T>.then(other: Path<T>): Path<T> {
    require(end == other.start) { "нельзя склеить пути: конец первого != начало второго" }
    return Path(start, "${label}; ${other.label}") { x -> other.proofFn(this.proofFn(x)) }
}

/**
 * явное доказательство того, что два параллеьлных пути (общие start,
 * общий end) гомотопны - то есть их функции-свидетели совпадают не только
 * в одной точке, а на целой выборке точек
 */
class TwoPath<T>(val left: Path<T>, val right: Path<T>, val sampleDomain: List<T>) {
    init {
        require(left.start == right.start) {
            "2-путь можно строить только между путями с общим началом"
        }
        require(left.end == right.end) {
            "2-путь можно строить только между путями с общим концом"
        }
        for (x in sampleDomain) {
            require(left.proofFn(x) == right.proofFn(x)) {
                "функции-свидетели расходятся в точке $x — эти пути НЕ гомотопны"
            }
        }
    }
    override fun toString(): String =
        "TwoPath: [${left.label}] ~ [${right.label}]  (проверено на ${sampleDomain.size} точках)"
}

fun example2HigherPaths() {
    println("=== Пример 2: путь как вычисление; 2-путь как явное доказательство ===")

    // Доказательство A: 1 -> 3 (+2) -> 9 (+6). Суммарно: +8.
    val viaThree = Path(1, "+2") { it + 2 }.then(Path(3, "+6") { it + 6 })
    // Доказательство B: 1 -> 4 (+3) -> 9 (+5). Суммарно тоже: +8.
    val viaFour = Path(1, "+3") { it + 3 }.then(Path(4, "+5") { it + 5 })

    println("viaThree = $viaThree")
    println("viaFour  = $viaFour")
    check(viaThree.start == viaFour.start && viaThree.end == viaFour.end)

    // Эти пути не равны просто потому, что совпадают их концы.
    // Чтобы связать их, нужно явно построить 2-путь
    val homotopy = TwoPath(viaThree, viaFour, sampleDomain = (-5..5).toList())
    println(homotopy)
    println("Оба пути реализуют x -> x + 8")

    val viaHack = Path(1, "хак: подогнан только под x=1") { x -> if (x == 1) 9 else x * 100 }
    println("viaHack  = $viaHack")
    check(viaHack.start == viaThree.start && viaHack.end == viaThree.end)

    var rejected = false
    try {
        TwoPath(viaThree, viaHack, sampleDomain = (-5..5).toList())
    } catch (e: IllegalArgumentException) {
        rejected = true
        println("Попытка построить TwoPath(viaThree, viaHack) отклонена: ${e.message}")
    }
    check(rejected) { "TwoPath не должен был принять эти пути как гомотопные" }

    println(
        "Вывод: совпадение конечных точек - это необходимое, но недостаточное\n" +
                "условие для \"равенства путей\". "
    )
}

fun main() {
    example1Univalence()
    example2HigherPaths()
}