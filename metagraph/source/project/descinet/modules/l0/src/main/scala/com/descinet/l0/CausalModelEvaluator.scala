package com.descinet.l0

import scala.reflect.runtime.universe._
import scala.tools.reflect.ToolBox
import scala.util.Random
import com.descinet.shared_data.types.Types._

class ModelEvaluator(
  externalMeasurements: Map[String, List[Measurement]],
  model: Model,
  time: Long
) {
  private val rng = new Random()
  private var evaluatedCache: Map[String, Double] = Map()
  private var evaluationStack: Set[String] = Set()
  private val toolbox = runtimeMirror(getClass.getClassLoader).mkToolBox()

  // Helper functions for randomness
  def randomDouble(): Double = rng.nextDouble()
  def randomGaussian(): Double = rng.nextGaussian()

  /**
   * Retrieves the latest observation value (Double) for a given external variable name (exolabel) at a specified time (time).
   * Assumes that the list of measurements for each exolabel is sorted in descending order by timestamp.
   * This assumption holds because the lists are reversed in the collectExternalMeasurementsForModel method.
   */
  def latest(exolabel: String, time: Long): Option[Double] = {
    externalMeasurements.get(exolabel).flatMap { measurements =>
      // Find the first measurement with a timestamp <= time
      measurements.find(_.timestamp <= time).map(_.value)
    }
  }

  /**
   * Retrieves the timestamp of the last observation for a given external variable name (exolabel) at a specified time (time).
   * Assumes that the list of measurements for each exolabel is sorted in descending order by timestamp.
   * This assumption holds because the lists are reversed in the collectExternalMeasurementsForModel method.
   */
  def latestTime(exolabel: String, time: Long): Option[Long] = {
    externalMeasurements.get(exolabel).flatMap { measurements =>
      // Find the first measurement with a timestamp <= time
      measurements.find(_.timestamp <= time).map(_.timestamp)
    }
  }

  // Evaluate an endogenous variable
  def evaluateEndogenous(label: String, t: Long): Double = {
    if (evaluationStack.contains(label)) {
      throw new RuntimeException(s"Cyclic dependency detected for variable $label")
    }

    evaluatedCache.getOrElse(label, {
      evaluationStack += label
      val equation = model.internalVariables(model.internalParameterLabels(label)).equation
      val result = evaluateEquation(equation, t, model.externalParameterLabels.keySet)
      evaluatedCache += (label -> result)
      evaluationStack -= label
      result
    })
  }

  // Parse and compile Scala code from string
  def evaluateEquation(equation: String, t: Long, externalLabels: Set[String]): Double = {
    val externalSymbols = externalLabels.map(label => s"val $label = \"$label\"").mkString("\n")
    val endogenousSymbols = model.internalParameterLabels.keys.map { label =>
      s"def $label(t: Long): Double = evaluateEndogenous(\"$label\", t)"
    }.mkString("\n")

    val code = s"""
      |(env: Any) => {
      |  import scala.math._
      |  val randomDouble = () => env.randomDouble() 
      |  val randomGaussian = () => env.randomGaussian() 
      |  val t = $t 
      |  $externalSymbols
      |  $endogenousSymbols
      |  $equation
      |}
    """.stripMargin

    val tree = toolbox.parse(code)
    val compiledCode = toolbox.compile(tree)
    compiledCode()(this).asInstanceOf[Double]
  }

  // Main evaluation process
  def evaluate(): Map[String, Double] = {
    model.internalVariables.zipWithIndex.map { case (variable, idx) =>
      val label = model.internalParameterLabels.collectFirst { case (key, `idx`) => key }.get
      val samples = (1 to 1000).map { _ =>
        evaluatedCache.getOrElse(label, {
          val result = evaluateEquation(variable.equation, time, model.externalParameterLabels.keySet)
          evaluatedCache += (label -> result)
          result
        })
      }
      label -> samples.sum / samples.size
    }.toMap
  }
}