
// define context
context{
    name = "TEST"
    plugin "immutable"
    plugin "plots"
    properties{
        a = 4
        b = false
    }
}

task(hep.dataforge.grind.TestTask)