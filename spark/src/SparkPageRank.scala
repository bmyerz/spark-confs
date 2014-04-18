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

import org.apache.spark.SparkContext._
import org.apache.spark.SparkContext


/**
 * Computes the PageRank of URLs from an input file. Input file should
 * be in format of:
 * URL         neighbor URL
 * URL         neighbor URL
 * URL         neighbor URL
 * ...
 * where URL and their neighbors are separated by space(s).
 */
object SparkPageRank {
  def timeStart() : Double = {
    return System.nanoTime();
  }
  def timeEnd(start:Double) : Double = {
    val end = System.nanoTime();
    return (end-start)/10e9
  }

  def main(args: Array[String]) {
    if (args.length < 3) {
      System.err.println("Usage: PageRank:) <master> <file> <tol>")
      System.exit(1)
    }
    var tol = args(2).toFloat
    val ctx = new SparkContext(args(0), "PageRank",
      System.getenv("SPARK_HOME"), SparkContext.jarOfClass(this.getClass))
    val lines = ctx.textFile(args(1), 1)
    val links = lines.map{ s =>
      val parts = s.split("\\s+")
      (parts(0), parts(1))
    }.distinct().groupByKey().cache()  // groupby forms edges lists
    var ranks = links.mapValues(v => 1.0)
    val activeStart = ranks

    val t = timeStart()
    var iter = 0
    var acount = activeStart.count()
    while (acount > 0) {
      val ti = timeStart()
      val contribs = links.join(ranks).values.flatMap{ case (urls, rank) =>
        val size = urls.size
        urls.map(url => (url, rank / size))
      }
      val newranks = contribs.reduceByKey(_ + _).mapValues(0.15 + 0.85 * _)
      val activeNow = ranks.zip(newranks).filter{ p =>
        val r = p._1;
        val nr = p._2;
        nr._2 - r._2 > tol
      }
      acount = activeNow.count()
      ranks = newranks;
      val timecum = timeEnd(t)
      val timeiter = timeEnd(ti)
      println("iteration "+iter+"\ttime="+timeiter+"\tcum="+timecum+"\tactive="+acount)

      iter +=1
    }

    val output = ranks.values.sum()
    println("sum of ranks="+output)

    System.exit(0)
  }
}

