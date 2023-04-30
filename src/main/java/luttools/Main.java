package luttools;

import org.apache.commons.cli.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class Main {
    private enum Mode {
        TEMPLATE("template"),
        EXTRACT_LUT("lut");

        private String modeName;

        Mode(String name) {
            this.modeName = name;
        }

        public static Mode getMode(String modeName) {
            for(Mode m : values())
                if(m.modeName.equals(modeName))
                    return m;

            return null;
        }

        @Override
        public String toString() {
            return modeName;
        }
    };

    private static final Option MODE_OPTION = Option.builder()
            .required(true)
            .option("m")
            .longOpt("mode")
            .hasArg(true)
            .argName("operating mode")
            .build();

    private static final Option INPUT_OPTION = Option.builder()
            .required(false)
            .option("i")
            .longOpt("input")
            .hasArg(true)
            .argName("input file")
            .build();

    private static final Option OUTPUT_OPTION = Option.builder()
            .required(true)
            .option("o")
            .longOpt("output")
            .hasArg(true)
            .argName("output file")
            .build();

    public static void main(String[] args) throws IOException {
        CommandLineParser parser    = new DefaultParser();
        HelpFormatter formatter     = new HelpFormatter();
        CommandLine cmd;

        Options options = createOptions();

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("luttool", options);

            System.exit(1);
            return;
        }

        Mode mode           = Mode.getMode(cmd.getOptionValue(MODE_OPTION));
        File inputFile      = cmd.hasOption(INPUT_OPTION) ? new File(cmd.getOptionValue(INPUT_OPTION)) : null;
        File outputFile     = new File(cmd.getOptionValue(OUTPUT_OPTION));

        validateArgs(formatter, cmd, options, mode, inputFile, outputFile);

        if(mode == Mode.TEMPLATE) {
            BufferedImage template = Utils.createLutTemplateImage();

            ImageIO.write(template, "png", outputFile);
        }
        else if(mode == Mode.EXTRACT_LUT) {
            BufferedImage template  = Utils.createLutTemplateImage();
            BufferedImage corrected = ImageIO.read(inputFile);

            Utils.ColorMapping[][][] lut = Utils.extractLutData(template, corrected);
            String cubeString = Utils.createCubeLut(lut, 32, outputFile.getName());

            FileWriter writer = new FileWriter(outputFile, false);
            writer.write(cubeString);
            writer.close();
        }

/*
        BufferedImage template = Utils.createLutTemplateImage();

        //ImageIO.write(template, "png", new File(System.getProperty("user.home"), "lut_template.png"));

        BufferedImage baseline  = ImageIO.read(new File(System.getProperty("user.home"), "lut_template.png"));
        BufferedImage corrected = ImageIO.read(new File(System.getProperty("user.home"), "lut_ektar25.png"));

        Utils.ColorMapping[][][] lutFromTemplate = Utils.extractLutData(template, corrected);
        Utils.ColorMapping[][][] lutFromBaseline = Utils.extractLutData(baseline, corrected);

        System.out.println(Arrays.deepEquals(lutFromBaseline, lutFromTemplate));
*/
    }

    private static void validateArgs(HelpFormatter formatter, CommandLine cmd, Options options, Mode mode, File inputFile, File outputFile) {
        if(mode == null) {
            System.out.println("Invalid mode specified. Available modes: " + Arrays.toString(Mode.values()));
            formatter.printHelp("luttool", options);

            System.exit(1);
        }

        if(mode == Mode.EXTRACT_LUT && (! inputFile.exists() || ! inputFile.isFile())) {
            System.out.println("Invalid input file: " + cmd.getOptionValue(INPUT_OPTION));
            formatter.printHelp("luttool", options);

            System.exit(1);
        }

        if(outputFile.isDirectory()) {
            System.out.println("Invalid output file (it's a directory): " + cmd.getOptionValue(OUTPUT_OPTION));
            formatter.printHelp("luttool", options);

            System.exit(1);
        }

        int outputDotIndex = outputFile.getName().lastIndexOf('.');
        String outputExtension = outputDotIndex <= 0 ? null : outputFile.getName().substring(outputDotIndex + 1);

        if(mode == Mode.TEMPLATE && (outputExtension == null || ! outputExtension.equals("png"))) {
            System.out.println("Output file's extension has to be 'png': " + cmd.getOptionValue(OUTPUT_OPTION));
            formatter.printHelp("luttool", options);

            System.exit(1);
        }

        int inputDotIndex = inputFile != null ? inputFile.getName().lastIndexOf('.') : -1;
        String inputExtension = inputDotIndex <= 0 ? null : inputFile.getName().substring(inputDotIndex + 1);

        if(mode == Mode.EXTRACT_LUT && (inputExtension == null || ! inputExtension.equals("png") || outputExtension == null || ! outputExtension.equals("cube"))) {
            System.out.println("Input file's extension has to be 'png': " + cmd.getOptionValue(INPUT_OPTION));
            System.out.println("Output file's extension has to be 'cube': " + cmd.getOptionValue(OUTPUT_OPTION));
            formatter.printHelp("luttool", options);

            System.exit(1);
        }
    }

    private static Options createOptions() {
        Options result = new Options();

        result.addOption(MODE_OPTION);
        result.addOption(INPUT_OPTION);
        result.addOption(OUTPUT_OPTION);

        return result;
    }
}
