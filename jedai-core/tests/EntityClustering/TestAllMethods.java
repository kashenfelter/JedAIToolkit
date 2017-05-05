/*
* Copyright [2016] [George Papadakis (gpapadis@yahoo.gr)]
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
 */
package EntityClustering;

import EntityMatching.*;
import BlockBuilding.*;
import Utilities.DataStructures.AbstractDuplicatePropagation;
import BlockProcessing.IBlockProcessing;
import Utilities.DataStructures.UnilateralDuplicatePropagation;
import DataModel.AbstractBlock;
import DataModel.EntityProfile;
import DataModel.EquivalenceCluster;
import DataModel.SimilarityPairs;
import DataReader.EntityReader.IEntityReader;
import DataReader.EntityReader.EntitySerializationReader;
import DataReader.GroundTruthReader.GtSerializationReader;
import DataReader.GroundTruthReader.IGroundTruthReader;
import Utilities.BlocksPerformance;
import Utilities.ClustersPerformance;
import Utilities.PrintToFile;
import Utilities.Enumerations.BlockBuildingMethod;
import Utilities.Enumerations.RepresentationModel;
import Utilities.Enumerations.SimilarityMetric;

import java.io.FileNotFoundException;
import java.util.List;

/**
 *
 * @author G.A.P. II
 */
public class TestAllMethods {

    public static void main(String[] args) {
        BlockBuildingMethod blockingWorkflow = BlockBuildingMethod.STANDARD_BLOCKING;

        String[] datasetProfiles = {
            "/home/ethanos/workspace/JedAIToolkitNew/datasets/restaurantProfiles", //            "E:\\Data\\csvProfiles\\censusProfiles",
        //            "E:\\Data\\csvProfiles\\coraProfiles",
        //            "E:\\Data\\csvProfiles\\cddbProfiles",
        //            "E:\\Data\\csvProfiles\\abt-buy\\dataset",
        //            "E:\\Data\\csvProfiles\\amazon-gp\\dataset",
        //            "E:\\Data\\csvProfiles\\dblp-acm\\dataset",
        //            "E:\\Data\\csvProfiles\\dblp-scholar\\dataset",
        //            "E:\\Data\\csvProfiles\\movies\\dataset"
        };
        String[] datasetGroundtruth = {
            "/home/ethanos/workspace/JedAIToolkitNew/datasets/restaurantIdDuplicates", //            "E:\\Data\\csvProfiles\\censusIdDuplicates",
        //            "E:\\Data\\csvProfiles\\coraIdDuplicates",
        //            "E:\\Data\\csvProfiles\\cddbIdDuplicates",
        //            "E:\\Data\\csvProfiles\\abt-buy\\groundtruth",
        //            "E:\\Data\\csvProfiles\\amazon-gp\\groundtruth",
        //            "E:\\Data\\csvProfiles\\dblp-acm\\groundtruth",
        //            "E:\\Data\\csvProfiles\\dblp-scholar\\groundtruth",
        //            "E:\\Data\\csvProfiles\\movies\\groundtruth"
        };

        for (int datasetId = 0; datasetId < datasetProfiles.length; datasetId++) {
            System.out.println("\n\n\n\n\nCurrent dataset id\t:\t" + datasetId);;

            IEntityReader eReader = new EntitySerializationReader(datasetProfiles[datasetId]);
            List<EntityProfile> profiles = eReader.getEntityProfiles();
            System.out.println("Input Entity Profiles\t:\t" + profiles.size());

            IGroundTruthReader gtReader = new GtSerializationReader(datasetGroundtruth[datasetId]);
            final AbstractDuplicatePropagation duplicatePropagation = new UnilateralDuplicatePropagation(gtReader.getDuplicatePairs(eReader.getEntityProfiles()));
            System.out.println("Existing Duplicates\t:\t" + duplicatePropagation.getDuplicates().size());

            double time1 = System.currentTimeMillis();
            
            IBlockBuilding blockBuildingMethod = BlockBuildingMethod.getDefaultConfiguration(blockingWorkflow);
            List<AbstractBlock> blocks = blockBuildingMethod.getBlocks(profiles, null);
            System.out.println("Original blocks\t:\t" + blocks.size());

            IBlockProcessing blockCleaningMethod = BlockBuildingMethod.getDefaultBlockCleaning(blockingWorkflow);
            if (blockCleaningMethod != null) {
                blocks = blockCleaningMethod.refineBlocks(blocks);
            }

            IBlockProcessing comparisonCleaningMethod = BlockBuildingMethod.getDefaultComparisonCleaning(blockingWorkflow);
            if (comparisonCleaningMethod != null) {
                blocks = comparisonCleaningMethod.refineBlocks(blocks);
            }

            double time2 = System.currentTimeMillis();
            
            BlocksPerformance blp = new BlocksPerformance(blocks, duplicatePropagation);
            blp.setStatistics();
            blp.printStatistics(time2-time1);

            RepresentationModel repModel = RepresentationModel.CHARACTER_BIGRAMS;
//            for (RepresentationModel repModel : RepresentationModel.values()) {
            System.out.println("\n\nCurrent model\t:\t" + repModel.toString() + "\t\t" + SimilarityMetric.getModelDefaultSimMetric(repModel));
            IEntityMatching em = new ProfileMatcher(repModel, SimilarityMetric.JACCARD_SIMILARITY);
            SimilarityPairs simPairs = em.executeComparisons(blocks, profiles);

            IEntityClustering ec = new RicochetSRClustering();
            ec.setSimilarityThreshold(0.1);
            List<EquivalenceCluster> entityClusters = ec.getDuplicates(simPairs);

            try {
                PrintToFile.toCSV(entityClusters, "/home/ethanos/workspace/JedAIToolkitNew/rest.csv");
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            ClustersPerformance clp = new ClustersPerformance(entityClusters, duplicatePropagation);
            clp.setStatistics();
            clp.printStatistics();
        }
//        }
    }
}
