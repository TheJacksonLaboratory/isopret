# isopret

Isoform Interpretation (isopret) is (will be) a tool to help interpret the potential biological
functions that are affected by differential alternative splicing.  Isopret is in a very early stage
right now...

## Building isopret
isopret was developed under Java 11. To build the app, clone this repository and
build the executable with maven.
```
git clone https://github.com/TheJacksonLaboratory/isopret
cd isopret
mvn package
```
This will create an executbale in the ``target`` subdirectory. Run the following command to make sure
the executable was created.
```
$ java -jar target/isopret.jar 
Usage: isopret [-hV] [COMMAND]
Isoform interpretation tool.
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  download, D  Download files for prositometry
  hbadeals, H  Analyze HBA-DEALS files
  svg, V       Create SVG/PDF files for a specific gene
  stats, S     Show descriptive statistics about data
```


## Download

isopret requires several files to run (NOTE--everything is being refactored, but this command works now). It will download these files and copy them
to a subdirectory called data with the ``download`` command. If desired, the paths
of the files can be adjusted, but by default prositometry will assume that all
required files are in the ``data`` directory.
```
java -jar target/isopret.jar download
```

## HBA-DEALS

After setting up the app and downloading the files, prositometry can be run with an HBA-DEALS output file that
includes transcript IDs. Additional options are available (TODO).
```
Usage: isopret hbadeals [-hV] [-a=<goGafFile>] -b=<hbadealsFile>
                        [-f=<fastaFile>] [-g=<goOboFile>] [-j=<jannovarPath>]
                        [-p=<prositeDataFile>] [--prefix=<outprefix>]
                        --prositemap=<prositeMapFile>
Analyze HBA-DEALS files
  -a, --gaf=<goGafFile>      goa_human.gaf.gz file
  -b, --hbadeals=<hbadealsFile>
                             HBA-DEALS output file
  -g, --go=<goOboFile>       go.obo file
  -h, --help                 Show this help message and exit.
  -j, --jannovar=<jannovarPath>
                             Path to Jannovar transcript file
  -p, --prosite=<prositeDataFile>
                             prosite.dat file
      --prefix=<outprefix>   Name of output file (without .html ending)
      --prositemap=<prositeMapFile>
                             prosite mape file
  -V, --version              Print version information and exit.
```

To get these files note the following

1. HBADEALS output -- please use with format that includes the ENSEMBL id in the first column (gene accession number).


### SVG

This is a convenience command that writes SVG files to disk for a specific gene. For instance,

```bazaar
java -jar target/isopret.jar svg -b <HBA-DEALS file>
--prositemap all_prosite_motifs.txt
--namespace ensembl
--prefix SMAD3 --gene SMAD3
```
This produces four files
 * SMAD3-isoforms.svg and SMAD3-isoforms.pdf    
 * SMAD3-protein.svg and SMAD3-protein.pdf  

THe PDF files are generated from the SVG files by running a system command with the ``rsvg-convert`` tool (which must be availble on the
system path for this to work. Otherwise, just SVG files will be generated).
