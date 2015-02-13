# dictionary-mangler
Code for taking Webster's dictionary, and using it to create a mapping from words to their part of speech.

This is part of a project that build off of the apache hadoop tutorial http://hadoop.apache.org/docs/current/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html I build it to soldify my knowegle of what the tutorial covers and to get expreince with hadoop features not used in their WordCount example e.g. the distributed cache. 
The program PrefixParser searches a text and returns all the string that are used as a prefix for a given infix.  E.g. "C" and "H" when used with infix "a" and the input text "The Cat in the Hat".  The DefintionParser creates a mapping between the words in Webster's (of length 2 or greater) and the parts of speech they can be, when it isused with the 1912 edition of Websters and my manually post processed PrefixParser output (see resources/PartsOfSpeech). From a programatic perspective it spilts a text that has some all caps tokens longer than 2 characters and some non-all caps words in to key value pairs of all caps words and all the text between them and the next all caps word.  Then for each pair it searchs the value for each word in the secondary input and creates a new key value pair with the original key and the word.
The command line argument format for the prefix parser program is input folder output folder with all inputs in the form of hdfs locations.  It also requires that you hardcode the infix properties.  The argument format for DefintionParser is input folder output folder keyword file. Please note that the first two are folders and the third is a file. The input folder can contain any text file(s) that you want analyzed. 

My other repository https://github.com/PatrickHunter/CorpusClassifier contians code that makes use of the classification file I generated with this projects. 
I would like to reiterate that while this program worked well for me its puprose was to teach me hadoop, and I make no gaurantees as to its fittness for anyother purpose.