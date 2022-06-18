# Sonata's Memory Model

Sonata's memory model is based on memory co-location and cache usage. Most of the time, Sonata
will decide what is the best tradeoff on memory usage, however, hints can be applied at the code level
to avoid some Sonata optimisations.

To understand how Sonata defines the memory, let's start with the memory object model.

## Sonata's Memory Object Model

Sonata defines memory models using two basic object types in a unified memory model.

* Static Objects
* Flexible Objects

### Static Objects

Static objects have a fixed size and can be embedded or directly mapped to memory regions to optimise memory usage and
access patterns. Example of static objects are numbers, enums, a booleans.

Value classes can be static objects, if, and only if, they are formed by other static objects. Example static value class:

```sn
value class Amount(value: double, currency: 'USD' | 'EUR')
```

### Flexible Objects

Flexible objects have an unbound size. This means that they can dynamically grow and shrink
depending on the business logic of your application.

Examples of flexible objects are strings, arrays, lists and trees.

Value classes are flexible if at least on or their members is also flexible.

```sn
value class Amount(value: double, currency: string)
```

## Internal Modeling

Internal modeling is the process of defining an optimized memory layout for a specific entity. Internal
modeling is a complex process, and it's always refined for better optimisations.

However, there are three principles in the internal modeling that are important:

* First: optimize for cache usage
* Second: optimize for memory footprint
* Third: optimize for most common cases

Internal modeling uses bit bags, direct mappings, references and paddings to implement the memory model.

### Bit Bags

Bit Bags are a set of sequential bits, padded to a byte, that are used to map static objects that are
smaller than a byte. For example, consider the following code:

```sn
value class Status(lastUpdate: date, value: 'OPEN' | 'CLOSED', public: boolean)
```

Values `value` and `public` can be mapped as two bits, as they are both boolean values internally.

| 1     | 2      | 3   | 4   | 5   | 6   | 7   | 8   |
|-------|--------|-----|-----|-----|-----|-----|-----|
| value | public | --- | --- | --- | --- | --- | --- |

By using a padding, we can fit two boolean values in a byte and align it to cacheable memory. However, consider
this following code now:

```sn
value class Category(lastUpdate: date, value: 'UNHEALTHY' | 'HEALTHY')
entity class Account(id: Id, category: Category, active: boolean, public: boolean)
```

Now we have some values inside a value object Category, and some values in the root of the entity. However, because
we have **consistent** and **semantic** mutation boundaries at the entity level, we can still remodel and optimise the memory layout and put
all fitting values in a single bit bag.

| 1              | 2      | 3      | 4   | 5   | 6   | 7   | 8   |
|----------------|--------|--------|-----|-----|-----|-----|-----|
| category.value | active | public | --- | --- | --- | --- | --- |

In case of an *overspill* the bit bag can grow to the next byte.

Enums can be mapped using a section of the bit bag bigger than a single bit, for example:

```sn
value class Semaphore(state: 'GREEN' | 'YELLOW' | 'RED')
```

| 1                     | 2       | 3   | 4   | 5   | 6   | 7   | 8   |
|-----------------------|---------|-----|-----|-----|-----|-----|-----|
| 0 = green, 1 = yellow | 1 = red | --- | --- | --- | --- | --- | --- |

### Direct Mapping

Direct mapping act as an embedded value. Static objects can always be embedded as they
have a fixed size. For example:

```sn
value class Amount(value: double, currency: 'USD' | 'EUR')
```

| 1        | 2   | 3   | 4   | 5   | 6   | 7   | 8   | 9-16  |
|----------|-----|-----|-----|-----|-----|-----|-----|-------|
| currency | --- | --- | --- | --- | --- | --- | --- | value |

Note the padding, to ensure that all direct mappings are padded at the byte level. If we have more elements
that can fit de bit bag, they will be optimised into the bit bag even if there are direct mappings.

```sn
value class Amount(value: double, currency: 'USD' | 'EUR', debt: boolean)
```

| 1        | 2     | 3   | 4   | 5   | 6   | 7   | 8   | 9-16  |
|----------|-------|-----|-----|-----|-----|-----|-----|-------|
| currency | debt  | --- | --- | --- | --- | --- | --- | value |
