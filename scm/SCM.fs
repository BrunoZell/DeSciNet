module SCM

open System

type IVariable =
    abstract member Value : obj

type IEvaluationContext =
    /// Get the current time t
    abstract member t : DateTime
    /// Get the last measurement of an exogenous variable
    abstract member latest : string -> obj
    /// Get the timestamp of the last measurement of an exogenous variable
    abstract member latestTime : string -> DateTime
    /// Get the current value of an endogenous variable
    abstract member value : string -> obj

// Define a type for probabilistic values
type ProbabilisticValue = 
    | Deterministic of obj
    | Probabilistic of (unit -> obj)

type EndogenousVariable = {
    Type: System.Type
    Equation: IEvaluationContext -> ProbabilisticValue
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
        member _.latestTime name =
            match j.TryFind(name) with
            | Some exoVar ->
                match exoVar.Measurements with
                | (latestTime, _) :: _ -> latestTime
                | [] -> failwithf "No measurements available for exogenous variable '%s'" name
            | None -> failwithf "Exogenous variable '%s' not found" name
        member self.value name = 
            match i.TryFind(name) with
            | Some endoVar -> 
                // Logic to get the current value using the equation
                match endoVar.Equation (self :> IEvaluationContext) with
                | Deterministic v -> v
                | Probabilistic f -> f()
            | None -> failwithf "Endogenous variable '%s' not found" name

let addMeasurement (variableName: string) (timestamp: DateTime) (value: obj) (j: Map<string, ExogenousVariable>) =
    match j.TryFind variableName with
    | Some exoVar ->
        let updatedMeasurements = (timestamp, value) :: exoVar.Measurements |> List.sortByDescending fst
        let updatedExoVar = { exoVar with Measurements = updatedMeasurements }
        j.Add(variableName, updatedExoVar)
    | None -> failwithf "Exogenous variable '%s' not found" variableName
