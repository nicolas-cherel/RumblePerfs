# rumblebars benches

While I took care in trying to build fair benches against others technologies, those should be considered as quick and dirty, the sole result I extract from those is that rust, while not being mature in a traditional sens, already allows good performances, on par with other technology. Even if rumblebars as an handlebars implementation is my first rust project, and I only looked for optimisation at design level, and very little at implementation level.

### comments
 - rumblebars does not compile the template, it just parses a data struct that is evaluated against data.
 - js and java implementation runs on JITed VMs, and my benches cases varies too little on templates and data to avoid those JIT compilers to optimise for the bench specific dataset.

## results

### rumblebars


```
test compile          ... bench:    188310 ns/iter (+/- 26068)
test compile_helper   ... bench:    320673 ns/iter (+/- 56388)
test expansion        ... bench:    830288 ns/iter (+/- 203735)
test helper_expansion ... bench:    960074 ns/iter (+/- 260606)
```

### handlebars js

```
test helper compile             ... bench:         6325 ns/iter (+/- 4885) — cold 140079 ns
test helper expansion           ... bench:       965696 ns/iter (+/- 846203) — cold 22644357 ns
test template compile           ... bench:         2740 ns/iter (+/- 327) — cold 5879 ns
test template expansion         ... bench:       353103 ns/iter (+/- 438287) — cold 5229538 ns
```

I just don't understand those compilation results. The v8 JIT compiler must be doing a great job.

### handlebars java (jknack)

```
test template compile           ... bench:       665583 ns/iter (+/- 631598) — cold 75833866 ns
test template expansion         ... bench:      1646359 ns/iter (+/- 642812) — cold 17839853 ns
test helper compile             ... bench:       587871 ns/iter (+/- 782877) — cold 2756260 ns
test helper expansion           ... bench:      1433598 ns/iter (+/- 273479) — cold 1532083 ns
```