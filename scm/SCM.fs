module SCM

open System

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
    Measurements: (DateTime * obj) list // sorted by timestamp, latest first at [0]
}

type I = Map<string, EndogenousVariable>
type J = Map<string, ExogenousVariable>

type EvaluationContext(t: DateTime, i: I, j: J) =
    interface IEvaluationContext with
        member _.t = t
        member _.latest name = 
            match j.TryFind(name) with
            | Some exoVar -> 
                // Logic to get the latest measurement
                match exoVar.Measurements with
                | (_, latestValue) :: _ -> latestValue
                | [] -> failwithf "No measurements available for exogenous variable '%s'" name
            | None -> failwithf "Exogenous variable '%s' not found" name
        member self.value name = 
            match i.TryFind(name) with
            | Some endoVar -> 
                // Logic to get the current value using the equation
                endoVar.Equation (self :> IEvaluationContext)
            | None -> failwithf "Endogenous variable '%s' not found" name

let addMeasurement (variableName: string) (timestamp: DateTime) (value: obj) (j: Map<string, ExogenousVariable>) =
    match j.TryFind variableName with
    | Some exoVar ->
        let updatedMeasurements = (timestamp, value) :: exoVar.Measurements |> List.sortByDescending fst
        let updatedExoVar = { exoVar with Measurements = updatedMeasurements }
        j.Add(variableName, updatedExoVar)
    | None -> failwithf "Exogenous variable '%s' not found" variableName
