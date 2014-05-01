/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.examples

import scala.util.Random
import org.apache.spark.{HashPartitioner, SparkContext}
import org.apache.spark.util.Vector
import org.apache.spark.SparkContext._
import java.util
import org.apache.spark.rdd.RDD

/**
 * K-means clustering.
 */
object SparkKMeans {
  def timeStart() : Double = {
    return System.nanoTime();
  }
  def timeEnd(start:Double) : Double = {
    val end = System.nanoTime();
    return (end-start)/1e9
  }

  def parseVector(line: String): Vector = {
    val burn = 2
    val delim = ","

    val features = line.split(delim)
    val arr = new Array[Double](features.length - burn)
    for (i <- burn until features.length) {
      arr(i-burn) = features(i).toDouble
    }

    new Vector(arr)
  }

  def elementWiseDivide(v1:Vector, v2:Vector): Vector = {
    val list = new Array[Double](v1.elements.length)
    for (i <- 0 to (v1.elements.length-1)) {
      list(i) =  v1(i) / v2(i)
    }
    new Vector(list)
  }

  def closestPointRR(p: Vector, centers: Array[Vector], offset: Int): Int = {
    return offset;
  }

  def closestPoint(p: Vector, centers: Array[Vector]/*, clusters_compared: Int*/): Int = {
    var index = 0
    var bestIndex = 0
    var closest = Double.PositiveInfinity

    //if (clusters_compared == 0) {

      for (i <- 0 until centers.length) {
        val tempDist = p.squaredDist(centers(i))
        if (tempDist < closest) {
          closest = tempDist
          bestIndex = i
        }
      }
    /*} else {
      for (i <- 0 until clusters_compared) {
        val tempDist = p.squaredDist(centers(Random.nextInt(centers.length))) // probably should sample without replacement
        if (tempDist < closest) {
          closest = tempDist
          bestIndex = i
        }
      }
    }*/

    bestIndex
  }

  def main(args: Array[String]) {
    if (args.length < 8) {
        System.err.println("Usage: SparkKMeans <master> <file> <k> <convergeDist> <normalize?> <maxiters> <RR> <force no combine?>")
        System.exit(1)
    }
    val sc = new SparkContext(args(0), "SparkKMeans",
      System.getenv("SPARK_HOME"), SparkContext.jarOfClass(this.getClass))
    val lines = sc.textFile(args(1))
    val data = lines.map(parseVector _).cache()
    val K = args(2).toInt
    val convergeDist = args(3).toDouble
    val maxiters = args(5).toInt
    val RR = args(6).toBoolean
    val force_no_combiner = args(7).toInt

    // normalize all features so they are weighted equally
    val sum = data.reduce(_ + _)
    var normalized_data = data;
    val numPoints = normalized_data.count()
    val numPartitions = normalized_data.partitioner.size
    val skipSize = (K.toDouble / numPartitions) // to help RR assignment be fair
    if (args(4).toBoolean) {
      println("normalizing")
      normalized_data = data.map( v => elementWiseDivide(v, sum))
    } else {
        println("not normalizing")
    }


    val kPoints = normalized_data.takeSample(withReplacement = false, K, 42).toArray
    println("features length = " + kPoints(0).length)
    var tempDist = Double.MaxValue

    val kmeans_start = timeStart()

    var iter = 0
    while((tempDist > convergeDist) && ((maxiters==0) || (iter < maxiters))) {
      val iter_start = timeStart()

      val closest =
        if (!RR) {
          normalized_data.map (p => (closestPoint(p, kPoints), (p, 1)))
        } else {
          noramlized_data.mapPartitionsWithIndex( {(a: Int, vals: Iterator[Vector]) =>
            var ctr = a * skipSize - 1
            vals.map{ p => ctr+=1; (closestPointRR(p, kPoints, ctr)%K, (p, 1)) }
          })
        }

      // this partitions the map values BEFORE reduceByKey so no local reductions are done
      // TODO: verify that spark lazy evaluation will not ellide this?
      if (force_no_combiner) {
        closest.partitionBy(new HashPartitioner(closest.partitions.size))
      }

      val pointStats = closest.reduceByKey{case ((x1, y1), (x2, y2)) => (x1 + x2, y1 + y2)}


      val newPoints = pointStats.map {pair => (pair._1, pair._2._1 / pair._2._2)}.collectAsMap()



      tempDist = 0.0
      for (i <- 0 until K) {
        tempDist += kPoints(i).squaredDist(newPoints(i))
      }
      
      for (newP <- newPoints) {
        kPoints(newP._1) = newP._2
      }
      val iter_runtime = timeEnd(iter_start)
      println("Finished iteration " + iter + " (delta = " + tempDist + ") (iter_runtime = " + iter_runtime + ")")
      iter+=1
    }

    val kmeans_runtime = timeEnd(kmeans_start)

    println("Final centers:")
    kPoints.foreach(println)
    println("kmeans_runtime " + kmeans_runtime)
    System.exit(0)
  }
}
