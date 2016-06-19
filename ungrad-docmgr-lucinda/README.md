# docmgr-lucinda
An [Elexis](http://www.elexis.info) document Manager based on [Lucinda](https://github.com/rgwch/ch.rgw.lucinda)

Status: Beta

## What it is

A document manager for Elexis. Differences compared to the default Omnivore-based document managers:

 * Adding of documents not only through the Plugin's interface, but also (and preferably) through external document aquisition systems, such as Mass-Document-Scanner software, E-Mail receiving software and others. In fact, any program that is able to write into a file system, can write to the Lucinda store.

 * Really fast search engine over all documents (not only the documents of the current patient). All documents are full text indexed when imported. Image documents, such as pdf's from a dumb scanner, are sent through an OCR system before indexing.

 * Documents live independently from Elexis and its database. Any program can find and read all documents.
 * Can also index and integrate Consultations, and documents from other sources such as Omnivore.

## How it works

Docmgr-lucinda is a GUI for [Lucinda](https://github.com/rgwch/Lucinda). If a Lucinda-Server is running somewhere in the network, it will connect.

## Prerequisites

[Elexis ungrad v 3.x](http://www.elexis.ch/ungrad). Probably, a legacy Elexis 3.1 OpenSource or newer will do as well.

## Installation and configuration

First install a [Lucinda-Server](https://github.com/rgwch/Lucinda/tree/master/lucinda-server) and check, if it's running correctly and if it can be reached over the network.

Then, install the docmgr-lucinda plugin: Launch Elexis and select `Help->Install new Software`from the menu. In the dropDown-Box "work with", select "Elexis ungrad addons". If this site is not listed, click the "Add" button and add a repository with name "elexis ungrad addons" and the location: `https://www.elexis.ch/ungrad/p2/features/latest`. There you should find an entry for "Document manager based on Lucinda".

After restart, you should find the Lucinda-settings page in Elexis' settings. Set the netmask to the correct value (e.g. `192.168.1.*`). The Lucinda server should be in the same Net. Leave the message prefix setting empty for now. Set the correct address for the Lucinda-Server.

## Usage

Open the lucinda view. The red dot in the upper left will turn green as soon, as the server is connected. If it does not connect by itself, click on the red dot to retry. If it does not turn green, check, if the Lucinda-Server is running, and if its address is set correctly in the plugin settings.
If the dot is green, you can enter some search terms and you'll see s list of documents matching your search. Click on an item to see its metadata. Double-Click a document to fetch and display it.

### Copyright notice

Lucinda, the Lucinda-Elexis-Plugin and the Lucinda Server are Copyright (c) 2016 by G. Weirich.
The Lucinda Icon ![Lucinda](icons/lucinda.gif) is created and copyrighted by http://www.fatcow.com/free-icons
