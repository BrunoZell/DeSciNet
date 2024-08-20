module JCM

type Variable = string * string

type Function = {
    Output: string
    Inputs: string list
    Expression: string
}

type SCM = {
    I: Variable list
    J: Variable list
    F: Function list
}

type IContext =
    abstract member t: System.DateTime
    abstract member latest: (string * System.DateTime) -> object
