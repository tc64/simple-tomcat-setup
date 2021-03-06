/* *****************************************************************************
 * Copyright (c) 2015-2019 Skymind, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

package hello;

import org.apache.commons.io.FilenameUtils;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.nd4j.linalg.api.memory.MemoryWorkspace;
import org.nd4j.linalg.api.memory.conf.WorkspaceConfiguration;
import org.nd4j.linalg.api.memory.enums.AllocationPolicy;
import org.nd4j.linalg.api.memory.enums.LearningPolicy;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.deeplearning4j.parallelism.ParallelInference;
import org.deeplearning4j.parallelism.inference.InferenceMode;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerScope;

public class CNNModel {

    private static final Logger logger = LoggerFactory.getLogger(CNNModel.class);
    public static final String DATA_PATH = FilenameUtils.concat(System.getProperty("java.io.tmpdir"), "dl4j_w2vSentiment/");
    private static final String WORD_VECTORS_PATH = FilenameUtils.concat(System.getProperty("java.io.tmpdir"), "GoogleNews-vectors-negative300.bin.gz");
    private static final int truncateReviewsToLength = 256;
    private static final int vectorSize = 300;
    private static int numServed = 0;
    private static long highesPhysBytesReached = 0;
    private static long highestTotalBytesReached = 0;
    private static long lastPhysBytes = 0;
    private static long lastTotalBytes = 0;
    private static WordVectors wordVectors;
    private static ComputationGraph net;
    //private static INDArray[] out;
    //private static INDArray input;
    
    private static ParallelInference pi;
    
    private static File f = new File(FilenameUtils.concat(System.getProperty("java.io.tmpdir"), "sentiment_cnn.model"));
    static {
    	System.out.println("static loading vectors");
    	wordVectors = CNNModel.getWordVectors();
    	System.out.println("static loading net");
    	try {
			net = ComputationGraph.load(f, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	turnOffPeriodicGc();
    	pi = new ParallelInference.Builder(net)
                // BATCHED mode is kind of optimization: if number of incoming requests is too high - PI will be batching individual queries into single batch. If number of requests will be low - queries will be processed without batching
                .inferenceMode(InferenceMode.BATCHED)

                // max size of batch for BATCHED mode. you should set this value with respect to your environment (i.e. gpu memory amounts)
                .batchLimit(32)

                // set this value to number of available computational devices, either CPUs or GPUs
                .workers(2)

                .build();

    }
    
    private static WorkspaceConfiguration initialConfig = WorkspaceConfiguration.builder()
            .initialSize(10 * 1024L * 1024L)
            .policyAllocation(AllocationPolicy.STRICT)
            .policyLearning(LearningPolicy.NONE)
            .build();
    
    private static INDArray getVector(String word) {
        INDArray vector;
        vector = CNNModel.wordVectors.getWordVectorMatrix(word);
        if (vector == null) {
        	vector =  CNNModel.wordVectors.
        			getWordVectorMatrix(CNNModel.wordVectors.vocab().wordAtIndex(0)).like();
        }
        return vector;
    }
        
    public static INDArray dsFromString(String sentence) {
    	List<String> tokens = Arrays.asList(sentence.split(" "));//tokenizeSentence(sentence);
        if(tokens.isEmpty())
            throw new IllegalStateException("No tokens available for input sentence - empty string or no words in vocabulary with RemoveWord unknown handling? Sentence = \"" +
                    sentence + "\"");
       
        // assumes format is Format.CNN2D
        int[] featuresShape = new int[] {1, 1, 0, 0};
        boolean sentencesAlongHeight = true; // TODO confirm this should be true
        if (sentencesAlongHeight) {
            featuresShape[2] = Math.min(truncateReviewsToLength, tokens.size());
            featuresShape[3] = vectorSize;
        } else {
            featuresShape[2] = vectorSize;
            featuresShape[3] = Math.min(truncateReviewsToLength, tokens.size());
        }

        INDArray features = Nd4j.create(featuresShape);
        int length = (sentencesAlongHeight ? featuresShape[2] : featuresShape[3]);
        INDArrayIndex[] indices = new INDArrayIndex[4];
        indices[0] = NDArrayIndex.point(0);
        indices[1] = NDArrayIndex.point(0);
        for (int i = 0; i < length; i++) {
            INDArray vector = getVector(tokens.get(i));

            if (sentencesAlongHeight) {
                indices[2] = NDArrayIndex.point(i);
                indices[3] = NDArrayIndex.all();
            } else {
                indices[2] = NDArrayIndex.all();
                indices[3] = NDArrayIndex.point(i);
            }

            features.put(indices, vector);
        }

        return features;
        
    }
    
    private static WordVectors getWordVectors() {
		WordVectors wordVectors = WordVectorSerializer.loadStaticModel(new File(WORD_VECTORS_PATH));
		return wordVectors;
	}
    
    public static void logBytesInfo() {
    	Long thisPhysBytes = Pointer.physicalBytes();
    	lastTotalBytes = Pointer.totalBytes();
    	
    	lastPhysBytes = thisPhysBytes;
    	
    	if (lastPhysBytes > highesPhysBytesReached) {
    		highesPhysBytesReached = lastPhysBytes;
    	}
    	
    	if (lastTotalBytes > highestTotalBytesReached) {
    		highestTotalBytesReached = lastTotalBytes;
    	}
    	
    	logger.info("num served: " + Integer.toString(numServed));
		logger.info("last phys bytes: " + Long.toString(lastPhysBytes));
		logger.info("last total bytes: " + Long.toString(lastTotalBytes));
		logger.info("highes phys bytes: " + Long.toString(highesPhysBytesReached));
		logger.info("highes total bytes: " + Long.toString(highestTotalBytesReached));
    	
    }
    
    public static String doInference(String txtToClassify) {
    	if (numServed % 1000 == 0) {
    		logger.info("before getting input");
        	logBytesInfo();
    	}
    	//try (@SuppressWarnings("unchecked")
		//PointerScope scope = new PointerScope()) {
    	try(MemoryWorkspace ws = Nd4j.getWorkspaceManager().getAndActivateWorkspace(initialConfig, "SOME_ID")) {
    		INDArray out;
    	    INDArray input;
    		input = dsFromString(txtToClassify);
    	
	    	if (numServed % 1000 == 0) {
		    	logger.info("after getting input");
		    	logBytesInfo();
	    	}
	    	out = CNNModel.pi.output(input);
        }
    		
	    //if (numServed % 10 == 0) 
	    //Pointer.attemptClearMemory(false, true, 1, 1);
	    //if (numServed % 1000 == 0) Pointer.attemptClearMemory(true, true, 10, 1);
	    	
    	if (numServed % 1000 == 0) {
	    	logger.info("after output");
	    	logBytesInfo();
	    	System.gc();
	    	logger.info("after system.gc");
    	}
    	
    	numServed += 1;
    	
    	return null;
    }
    
    public static int numServed() {
    	return numServed;
    }
    
    public static long lastPhysBytes() {
    	return lastPhysBytes;
    }
    
    public static long lastTotalBytes() {
    	return lastTotalBytes;
    }
    
    public static long physBytesNow() {
    	return Pointer.physicalBytes();
    }
    
    public static long totalBytesNow() {
    	return Pointer.totalBytes();
    }
    
    public static long highestPhysBytes() {
    	return highesPhysBytesReached;
    }
    
    public static void turnOnPeriodicGc() {
    	Nd4j.getMemoryManager().togglePeriodicGc(true);
    }
    
    public static void turnOffPeriodicGc() {
    	Nd4j.getMemoryManager().togglePeriodicGc(false);
    }
    
    public static void cleanDefault() {
    	//Pointer.attemptClearMemory(true, true, 1, 1);
    }

    public static void cleanTrimOnly() {
    	//Pointer.attemptClearMemory(false, true, 1, 1);
    }
    
    public static void main(String[] args) {
    	Integer numIter = Integer.parseInt(args[0]);
    	String test = "The movie was terrible, i would not recommend it to my worst enemy";
    	for (int i = 0; i < numIter;i++) {
    		String res = CNNModel.doInference(test);
    	}
    }
    
}
