// Generate the quickest path based on observations
let submitQuickestPath (observations: Observation<Position> list) (airports: Airport list) : SubmittedPaths<Position> =
    observations
    |> List.pairwise
    |> List.fold (fun paths (startObs, endObs) ->
        let path =
            if distance startObs.Variable endObs.Variable < 1000.0 then
                [walkIntervention endObs.Variable]  // Walk if within 1 km
            else
                [flyIntervention airports]  // Fly if farther than 1 km
        submitPath startObs endObs path paths
    ) []