
// define context
context{
    name = "myTestContext"
    plugin "plots"
    properties{
        a = 4
        b = false
    }
}

loadTask(hep.dataforge.grind.TestTask)

