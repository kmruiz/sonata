$mainClass="io.sonata.lang.Bootstrap"
$jar="target/lang-1.0-SNAPSHOT-jar-with-dependencies.jar"

function sample {
    param ([String] $output, [String] $script, [String] $requires = "samples/")

    echo "Compiling $script to $output with $requires"
    (java -jar $jar $params -o $output -r $requires $script) | out-null
    echo "Running $output"
    node $output
}

./mvnw package

sample samples/bank/example.js samples/bank/example.sn
sample samples/dev/disemvowel.js samples/dev/disemvowel.sn
sample samples/entity-concurrency/ping-pong.js samples/entity-concurrency/ping-pong.sn
sample samples/fibonacci/fibonacci.js samples/fibonacci/fibonacci.sn
sample samples/fibonacci/fibonacci-with-if.js samples/fibonacci/fibonacci-with-if.sn
sample samples/module/main.js samples/module/main.sn samples/module/
sample samples/simple/array.js samples/simple/array.sn
sample samples/simple/array-with-tail.js samples/simple/array-with-tail.sn
sample samples/simple/map.js samples/simple/map.sn
sample samples/streams/random.js samples/streams/random.sn
sample samples/types/function-overload-by-type.js samples/types/function-overload-by-type.sn
sample samples/types/value-class.js samples/types/value-class.sn
sample samples/types/value-class-destructuring.js samples/types/value-class-destructuring.sn