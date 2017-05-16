package gpcuster.kaggle.houseprice

import org.apache.spark.ml.Transformer
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

object Predictor {
  def getOutputDF(inputDF: DataFrame, model: Transformer): DataFrame = {
    val prediction = model.transform(inputDF)

    val negativePredictionCount = prediction.where("prediction <= 0").count()
    println("Prediction Data Set Negative Prediction Count: " + negativePredictionCount)

    val convertPrediction = udf {
      prediction: Double => prediction match {
        case  survived if survived > 0 => 1
        case _ => 0
      }
    }

    println(prediction.schema)

    // use abs to make sure the price is positive.
    val outputDF = prediction.withColumn("SalePrice", exp(abs(col("prediction"))) - 1).select("Id", "SalePrice")

    outputDF
  }
}
