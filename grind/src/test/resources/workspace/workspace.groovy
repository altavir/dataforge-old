
// define context
context{
    name = "myTestContext"
    plugin "cache"
    properties{
        a = 4
        b = false
    }
}

task(hep.dataforge.grind.TestTask)

