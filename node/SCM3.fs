module SCM3

open System
open System.Collections.Generic

type Variable<'T> = 
    { Name: string
      mutable Value: 'T option
      mutable Equation: (unit -> 'T) option }

type Measurement<'T> = 
    { Variable: Variable<'T>
      Value: 'T
      Timestamp: DateTime }

type CausalModel<'T> = 
    { Variables: Map<string, Variable<'T>>
      Measurements: List<Measurement<'T>> }

module CausalModel =

    let create () = 
        { Variables = Map.empty
          Measurements = List() }

    let addVariable (model: CausalModel<'T>) (name: string) (initialValue: 'T option) (equation: (unit -> 'T) option) =
        let variable = { Name = name; Value = initialValue; Equation = equation }
        { model with Variables = model.Variables.Add(name, variable) }

    let integrateMeasurement (model: CausalModel<'T>) (name: string) (value: 'T) (timestamp: DateTime) =
        match model.Variables.TryFind(name) with
        | Some variable ->
            let measurement = { Variable = variable; Value = value; Timestamp = timestamp }
            variable.Value <- Some value
            { model with Measurements = measurement :: model.Measurements }
        | None -> failwithf "Variable %s not found" name

    let doIntervention (model: CausalModel<'T>) (name: string) (equation: unit -> 'T) =
        match model.Variables.TryFind(name) with
        | Some variable ->
            variable.Equation <- Some equation
            model
        | None -> failwithf "Variable %s not found" name

    let computeOutcome (model: CausalModel<'T>) (names: string list) (timestamp: DateTime) : Map<string, 'T option> =
        let computeVariable (variable: Variable<'T>) =
            match variable.Equation with
            | Some equation -> Some (equation ())
            | None -> variable.Value

        names
        |> List.map (fun name -> 
            match model.Variables.TryFind(name) with
            | Some variable -> name, computeVariable variable
            | None -> failwithf "Variable %s not found" name)
        |> Map.ofList
