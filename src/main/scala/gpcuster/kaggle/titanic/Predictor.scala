package gpcuster.kaggle.titanic

import gpcuster.kaggle.util.SparkUtils
import org.apache.spark.ml.classification.{DecisionTreeClassifier, LogisticRegression, NaiveBayes, RandomForestClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.{Estimator, Transformer}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object Predictor {
  def getOutputDF(model: Transformer): DataFrame = {
    val inputPath = "src/main/resources/data/titanic/test.csv"

    val customSchema = StructType(Array(
      StructField("PassengerId", IntegerType, false),
      //StructField("Survived", IntegerType, false),
      StructField("Pclass", IntegerType, false),
      StructField("Name", StringType, false),
      StructField("Sex", StringType, false),
      StructField("Age", DoubleType, true),
      StructField("SibSp", IntegerType, false),
      StructField("Parch", IntegerType, false),
      StructField("Ticket", StringType, false),
      StructField("Fare", DoubleType, false),
      StructField("Cabin", StringType, false),
      StructField("Embarked", StringType, false)
    )
    )

    val inputDF = SparkUtils.getSpark().read.format("com.databricks.spark.csv")
      .option("header", "true") // Use first line of all files as header
      .schema(customSchema)
      .load(inputPath)

    val prediction = model.transform(inputDF)

    prediction.createOrReplaceTempView("outputTable")

    val convertPrediction = udf {
      prediction: Double => prediction match {
        case  survived if survived > 0 => 1
        case _ => 0
      }
    }

    val outputDF = prediction.withColumn("Survived", convertPrediction(col("prediction"))).select("PassengerId", "Survived")

    outputDF
  }
}
