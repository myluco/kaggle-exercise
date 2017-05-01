package gpcuster.kaggle.titanic

import java.text.SimpleDateFormat

import gpcuster.kaggle.util.SparkUtils
import org.apache.spark.sql.types._

object App {
  def main(args: Array[String]): Unit = {

    val trainingPath = "src/main/resources/data/titanic/train.csv"

    val trainingSchema = StructType(Array(
      StructField("PassengerId", IntegerType, false),
      StructField("Survived", IntegerType, false),
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

    val trainingDF = SparkUtils.readCSV(trainingPath, trainingSchema)

    trainingDF.createOrReplaceTempView("inputTable")

    trainingDF.show(10, false)

    SparkUtils.sql("select * from inputTable where age is null")

    SparkUtils.sql("select * from inputTable where survived = 1")

    val model = Modeler.getModel(trainingDF)

    val testingPath = "src/main/resources/data/titanic/test.csv"

    val testingSchema = StructType(Array(
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

    val testingDF = SparkUtils.readCSV(testingPath, testingSchema)

    val outputDF = Predictor.getOutputDF(testingDF, model)

    val df:SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
    val runId:String = df.format(System.currentTimeMillis())

    SparkUtils.writeCSV("output/titanic/" + runId, outputDF)
  }
}
