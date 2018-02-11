
// define context
context{
    name = "TEST"
    plugin "cache"
    plugin "plots-fx"
    properties{
        a = 4
        b = false
    }
}

task(hep.dataforge.grind.TestTask)