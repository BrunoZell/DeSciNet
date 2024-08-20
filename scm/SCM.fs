module SCM3

open System
open System.Collections.Generic

type IVariable =
    abstract member Value : obj

type IEvaluationContext =
    /// Get the current time t
    abstract member t : DateTime
    /// Get the last measurement of an exogenous variable
    abstract member latest : string -> obj
    /// Get the current value of an endogenous variable
    abstract member value : string -> obj

type EndogenousVariable = {
    Type: System.Type
    Equation: IEvaluationContext -> obj
}

type ExogenousVariable = {
    Type: System.Type
    Measurements: Map<DateTime, obj>
}

let i : Map<string, EndogenousVariable> = Map.empty
let j : Map<string, ExogenousVariable> = Map.empty
