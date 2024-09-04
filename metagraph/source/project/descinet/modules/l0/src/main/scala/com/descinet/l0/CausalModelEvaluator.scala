package com.descinet.l0

import scala.reflect.runtime.universe._
import scala.tools.reflect.ToolBox
import scala.util.Random
import com.descinet.shared_data.types.Types._

/**
 * Trait defining the callback interface for dynamic code evaluation.
 * This trait provides methods for generating random numbers, retrieving
 * the latest measurements, and evaluating endogenous variables.
 */
trait EvaluationContext {
  /**
   * Generates a random double value.
   * @return A random double value between 0.0 and 1.0.
   */
  def randomDouble(): Double

  /**
   * Generates a random Gaussian (normally distributed) value.
   * @return A random Gaussian value with mean 0.0 and standard deviation 1.0.
   */
  def randomGaussian(): Double

  /**
   * Retrieves the latest observation value (Double) for a given external variable name (exolabel) at a specified time (time).
   * Assumes that the list of measurements for each exolabel is sorted in descending order by timestamp.
   * @param exolabel The name of the external variable.
   * @param time The specified time to retrieve the latest observation.
   * @return An Option containing the latest observation value if available, otherwise None.
   */
  def latest(exolabel: String, time: Long): Option[Double]

  /**
   * Retrieves the timestamp of the last observation for a given external variable name (exolabel) at a specified time (time).
   * Assumes that the list of measurements for each exolabel is sorted in descending order by timestamp.
   * @param exolabel The name of the external variable.
   * @param time The specified time to retrieve the latest timestamp.
   * @return An Option containing the latest timestamp if available, otherwise None.
   */
  def latestTime(exolabel: String, time: Long): Option[Long]

  /**
   * Evaluates an endogenous variable at a given time.
   * @param label The label of the endogenous variable.
   * @param t The time at which to evaluate the endogenous variable.
   * @return The evaluated value of the endogenous variable.
   */
  def evaluateEndogenous(label: String, t: Long): Double
}

class ModelEvaluator(
  externalMeasurements: Map[String, List[Measurement]],
  model: Model,
  time: Long
) extends EvaluationContext {
  private val rng = new Random()
  private var evaluationStack: Set[String] = Set()
  private val toolbox = runtimeMirror(getClass.getClassLoader).mkToolBox()

  // Implement the EvaluationContext methods
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

    evaluationStack += label
    val equation = model.internalVariables(model.internalParameterLabels(label)).equation
    val result = evaluateEquation(equation, t, model.externalParameterLabels.keySet)
    evaluationStack -= label
    result
  }

  // Parse and compile Scala code from string
  def evaluateEquation(equation: String, t: Long, externalLabels: Set[String]): Double = {
    val externalSymbols = externalLabels.map(label => s"val $label = \"$label\"").mkString("\n")
    val endogenousSymbols = model.internalParameterLabels.keys.map { label =>
      s"def $label(t: Long): Double = env.evaluateEndogenous(\"$label\", t)"
    }.mkString("\n")

    val code = s"""
      |(env: com.descinet.l0.EvaluationContext) => {
      |  import scala.math._
      |  def randomDouble(): Double = env.randomDouble() 
      |  def randomGaussian(): Double = env.randomGaussian() 
      |  def latest(exolabel: String, t: Long): Option[Double] = env.latest(exolabel, t) 
      |  def latestTime(exolabel: String, t: Long): Option[Long] = env.latestTime(exolabel, t) 
      |  val t: Long = $t 
      |  $externalSymbols
      |  $endogenousSymbols
      |  $equation
      |}
    """.stripMargin

    val tree = toolbox.parse(code)
    val compiledCode = toolbox.compile(tree)
    val function = compiledCode().asInstanceOf[EvaluationContext => Double]
    function(this)
  }

  // Main evaluation process
  def evaluate(): Map[String, Double] = {
    model.internalVariables.zipWithIndex.map { case (variable, idx) =>
      val label = model.internalParameterLabels.collectFirst { case (key, `idx`) => key }.get
      val samples = (1 to 1000).map { _ =>
        evaluateEquation(variable.equation, time, model.externalParameterLabels.keySet)
      }
      label -> samples.sum / samples.size
    }.toMap
  }
}