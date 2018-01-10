package emufog.launcher;

import com.beust.jcommander.JCommander;
import emufog.backbone.BackboneClassifier;
import emufog.docker.FogType;
import emufog.export.CoupledMaxiNetExporter;
import emufog.export.IGraphExporter;
import emufog.fog.FogNodeClassifier;
import emufog.fog.FogResult;
import emufog.graph.Graph;
import emufog.graph.Node;
import emufog.images.IApplicationImageAssignmentPolicy;
import emufog.images.MongoCaseAssignmentPolicy;
import emufog.reader.GraphReader;
import emufog.settings.Settings;
import emufog.util.Logger;
import emufog.util.LoggerLevel;
import emufog.util.Tuple;

import java.io.IOException;
import java.nio.file.Paths;

import static emufog.launcher.ArgumentHelpers.getReader;

/**
 * The EmuFog main launcher class. Starts a new instance of the application with the given parameters
 * by the command line interface.
 */
public class Emufog {

    /**
     * Main function call to start EmuFog.
     *
     * @param args arguments of the command line
     */
    public static void main(String[] args) {
        // logger to write to log file and command line
        Logger logger = Logger.getInstance();
        logger.logSeparator();
        logger.log("Welcome to EmuFog");
        logger.logSeparator();
        Arguments arguments = new Arguments();
        Graph graph;


        try {
            // parse the command line arguments
            JCommander.newBuilder().addObject(arguments).build().parse(args);


            Settings settings = new Settings().read(arguments.settingsPath);

            // determines the respective format reader
            GraphReader reader = getReader(arguments.inputType, settings);

            // read in the graph with the graph reader
            long start = System.nanoTime();
            graph = reader.readGraph(arguments.files);
            long end = System.nanoTime();

            logger.log("Time to read in the graph: " + Logger.convertToMs(start, end));
            logger.logSeparator();
            // print graph details for information purposes
            logger.log("Number of nodeconfig in the graph: " + graph.getRouters().size());
            logger.log("Number of edges in the graph: " + graph.getEdges().size());
            logger.logSeparator();

            // compute the backbone of the network
            start = System.nanoTime();
            BackboneClassifier.identifyBackbone(graph);
            end = System.nanoTime();
            logger.log("Time to determine the backbone of the topology: " + Logger.convertToMs(start, end));
            logger.logSeparator();
            logger.log("Number of backbone nodeconfig identified: " + graph.getSwitches().size());
            logger.logSeparator();

            // assign devices to the edge
            graph.assignEdgeDevices();

            // find the fog node placements
            FogResult result = new FogNodeClassifier(settings).findFogNodes(graph);
            if (result.getStatus()) {
                for (Tuple<Node, FogType> tuple : result.getFogNodes()) {
                    graph.placeFogNode(tuple.getKey(), tuple.getValue());
                }
                IApplicationImageAssignmentPolicy policy = new MongoCaseAssignmentPolicy();
                policy.generateImageMapping(graph, settings);
                policy.generateCommandsLists(graph, settings);
                IGraphExporter exporter = new CoupledMaxiNetExporter();
                exporter.exportGraph(graph, Paths.get(arguments.output));
            } else {
                // no fog placement found, aborting
                logger.log("Unable to find a fog placement with the provided settings.", LoggerLevel.ERROR);
                logger.log("Consider using different settings.", LoggerLevel.ERROR);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.log("An exception stopped EmuFog!", LoggerLevel.ERROR);
            logger.log("Error message: " + e.getMessage(), LoggerLevel.ERROR);
        } finally {
            logger.log("Closing EmuFog");
        }
    }

}
