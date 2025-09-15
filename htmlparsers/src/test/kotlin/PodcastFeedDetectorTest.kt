/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2025 by Frederic-Charles Barthelery.
 *
 * This file is part of Geekttrss.
 *
 * Geekttrss is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Geekttrss is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Geekttrss.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.geekorum.ttrss.htmlparsers

import com.google.common.truth.Truth
import kotlin.test.BeforeTest
import kotlin.test.Test

private val validFeed = """
    <?xml version="1.0" encoding="UTF-8"?>
    <?xml-stylesheet type="text/xsl" href="rss.xslt" ?>
    <rss
        xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd"
        xmlns:googleplay="http://www.google.com/schemas/play-podcasts/1.0"
        xmlns:content="http://purl.org/rss/1.0/modules/content/"
        xmlns:atom="http://www.w3.org/2005/Atom"
        xmlns:spotify="http://www.spotify.com/ns/rss"
        xmlns:psc="http://podlove.org/simple-chapters/"
        xmlns:media="https://search.yahoo.com/mrss/"
        xmlns:podcast="https://podcastindex.org/namespace/1.0"
        version="2.0">
        <channel>
            <title>AyDiYoSa : Talking To The People</title>
                        <link>https://www.aydiyosa.com/</link>
                    <atom:link rel="self" type="application/rss+xml" href="https://feed.ausha.co/Bqrwj0szAmGY"/>
            <description>
    AyDiYoSa est un podcast 99% avis tranché dans lequel Gérald JOSEPH-ALEXANDRE fondateur de Platypus Agency et Tann Audio, digital nomade, discutera de sujets digitaux, e-commerce, de voyage ou de food.
    Au travers d'interviews ou de talks débat, les invité.e.s de tous horizons échangeront autour de leur parcours, de leur histoire, partageront leur expérience, donneront leur avis tranché sur les sujets abordés.
    AyDiYoSa se veut ouvert, inspirant, factuel et souhaite donner l'envie à ceux qui l'écouteront, eux aussi, d'affirmer ce qui les fait vibrer ou pas.
    « Je suis un grand voyageur, dans ce podcast digital nomade, je partagerai les folles rencontres, la découverte de personnes aux parcours inspirants.
    Le voyage est pour moi un parcours. 🚀» 

    Découvrez “AyDiYoSa : Talking To The People”, le podcast digital nomade qui explore les mondes du voyage, de l’e-commerce, du digital et de la gastronomie à travers des discussions inspirantes et enrichissantes.
    Hébergé par Ausha. Visitez ausha.co/fr/politique-de-confidentialite pour plus d'informations.</description>
            <language>fr</language>
            <copyright>Gérald JOSEPH-ALEXANDRE</copyright>
            <lastBuildDate>Tue, 11 Feb 2025 06:01:18 +0000</lastBuildDate>
            <pubDate>Tue, 11 Feb 2025 06:01:18 +0000</pubDate>
            <webMaster>feeds@ausha.co (Ausha)</webMaster>
            <generator>Ausha (https://www.ausha.co)</generator>
                        <spotify:countryOfOrigin>fr</spotify:countryOfOrigin>
            
            <itunes:author>Gérald JOSEPH-ALEXANDRE</itunes:author>
            <itunes:owner>
                <itunes:name>Gérald JOSEPH-ALEXANDRE</itunes:name>
                <itunes:email>Gerald@tann.audio</itunes:email>
            </itunes:owner>
            <itunes:summary>
    AyDiYoSa est un podcast 99% avis tranché dans lequel Gérald JOSEPH-ALEXANDRE fondateur de Platypus Agency et Tann Audio, digital nomade, discutera de sujets digitaux, e-commerce, de voyage ou de food.
    Au travers d'interviews ou de talks débat, les invité.e.s de tous horizons échangeront autour de leur parcours, de leur histoire, partageront leur expérience, donneront leur avis tranché sur les sujets abordés.
    AyDiYoSa se veut ouvert, inspirant, factuel et souhaite donner l'envie à ceux qui l'écouteront, eux aussi, d'affirmer ce qui les fait vibrer ou pas.
    « Je suis un grand voyageur, dans ce podcast digital nomade, je partagerai les folles rencontres, la découverte de personnes aux parcours inspirants.
    Le voyage est pour moi un parcours. 🚀» 
    Découvrez “AyDiYoSa : Talking To The People”, le podcast digital nomade qui explore les mondes du voyage, de l’e-commerce, du digital et de la gastronomie à travers des discussions inspirantes et enrichissantes.
    Hébergé par Ausha. Visitez ausha.co/fr/politique-de-confidentialite pour plus d'informations.</itunes:summary>
            <itunes:explicit>false</itunes:explicit>
            <itunes:block>no</itunes:block>
            <podcast:block>no</podcast:block>
            <podcast:locked>yes</podcast:locked>
            <itunes:type>episodic</itunes:type>
                        <itunes:subtitle>Talking To The People</itunes:subtitle>
                    
            <googleplay:author>Gérald JOSEPH-ALEXANDRE</googleplay:author>
            <googleplay:email>Gerald@tann.audio</googleplay:email>
            <googleplay:description>
    AyDiYoSa est un podcast 99% avis tranché dans lequel Gérald JOSEPH-ALEXANDRE fondateur de Platypus Agency et Tann Audio, digital nomade, discutera de sujets digitaux, e-commerce, de voyage ou de food.
    Au travers d'interviews ou de talks débat, les invité.e.s de tous horizons échangeront autour de leur parcours, de leur histoire, partageront leur expérience, donneront leur avis tranché sur les sujets abordés.
    AyDiYoSa se veut ouvert, inspirant, factuel et souhaite donner l'envie à ceux qui l'écouteront, eux aussi, d'affirmer ce qui les fait vibrer ou pas.
    « Je suis un grand voyageur, dans ce podcast digital nomade, je partagerai les folles rencontres, la découverte de personnes aux parcours inspirants.
    Le voyage est pour moi un parcours. 🚀» 
    Découvrez “AyDiYoSa : Talking To The People”, le podcast digital nomade qui explore les mondes du voyage, de l’e-commerce, du digital et de la gastronomie à travers des discussions inspirantes et enrichissantes.
    Hébergé par Ausha. Visitez ausha.co/fr/politique-de-confidentialite pour plus d'informations.</googleplay:description>
            <googleplay:explicit>false</googleplay:explicit>

                        <podcast:funding url="">Support us!</podcast:funding>
            
            <category>Society &amp; Culture</category>
        
            <itunes:category text="Society &amp; Culture">
                        <itunes:category text="Places &amp; Travel"/>
                </itunes:category>
            <category>Technology</category>
        
                <itunes:category text="Technology"/>
        
                        <image>
                    <url>https://image.ausha.co/fz2K5TbaeJ4wJqIvp6beWCYUCLCzeeVHxILT43Jt_1400x1400.jpeg?t=1692870043</url>
                    <title>AyDiYoSa : Talking To The People</title>
                                        <link>https://www.aydiyosa.com/</link>
                                </image>
                <itunes:image href="https://image.ausha.co/fz2K5TbaeJ4wJqIvp6beWCYUCLCzeeVHxILT43Jt_1400x1400.jpeg?t=1692870043"/>
                <googleplay:image href="https://image.ausha.co/fz2K5TbaeJ4wJqIvp6beWCYUCLCzeeVHxILT43Jt_1400x1400.jpeg?t=1692870043"/>
            
                        <item>
                    <title>TASTE IT - Voyage en Amazonie avec Amanda qui vient DAMAZONIE</title>
                    <guid isPermaLink="false">c2012f451adf7979ffe3fc039150cea5588777ff</guid>
                    <description><![CDATA[<p>Finalement c'est le deuxième VRAI épisode du format TASTE IT dans lequel je vous fait voyager avec moi. </p><p><br></p><p>Lors de mon troisième voyage en Guyane française j'ai fait la connaissance d'Amanda comme elle dit elle même elle se sent profondément " D'Amazonie" et c'est aussi le nom de son profil Instagram  <a href="https://www.instagram.com/damazonie_/">https://www.instagram.com/damazonie_/</a>). </p><p><br></p><p>Son truc à elle c'est le voyage, c'est la Guyane c'est faire découvrir chez elle son pays mais au delà de ça de faire découvrir l'Amazonie tout entière. </p><p><br></p><p>Elle a déjà voyagé dans plus d’une quinzaine de pays notamment en Asie comme le cambodge, l’Inde ou le Sri Lanka, mais elle l'a fait dans un autre bout de l’Amazonie avec un voyage de presque 2 mois entre Brésil, Pérou, Bolivie et Chili.</p><p><br></p><p>Ce road road trip qui l'a marquée à tout jamais. Elle a accepté de partager une partie de ses péripéties. </p><p><br></p><p>Dans cet épisode je suis accompagné de mon ami Lova Jah lui aussi guyanais et qui a créé "l'hymne" de la Guyane, La Gwiyan Bel. <br><br>Découvrez <b>“AyDiYoSa : Talking To The People”</b>, le <b>podcast digital nomade</b> qui explore les mondes du voyage, de l’e-commerce, du digital et de la gastronomie à travers des discussions inspirantes et enrichissantes.</p><p><br></p><p>« Je suis un grand voyageur, dans ce podcast digital nomade, je partagerai les folles rencontres, la découverte de personnes aux parcours inspirants.</p><p>Le voyage est pour moi un parcours. 🚀 » Gérald JOSEPH-ALEXANDRE</p><br/><p>Hébergé par Ausha. Visitez <a href="https://ausha.co/politique-de-confidentialite">ausha.co/politique-de-confidentialite</a> pour plus d'informations.</p>]]></description>
                    <content:encoded><![CDATA[<p>Finalement c'est le deuxième VRAI épisode du format TASTE IT dans lequel je vous fait voyager avec moi. </p><p><br></p><p>Lors de mon troisième voyage en Guyane française j'ai fait la connaissance d'Amanda comme elle dit elle même elle se sent profondément " D'Amazonie" et c'est aussi le nom de son profil Instagram  <a href="https://www.instagram.com/damazonie_/">https://www.instagram.com/damazonie_/</a>). </p><p><br></p><p>Son truc à elle c'est le voyage, c'est la Guyane c'est faire découvrir chez elle son pays mais au delà de ça de faire découvrir l'Amazonie tout entière. </p><p><br></p><p>Elle a déjà voyagé dans plus d’une quinzaine de pays notamment en Asie comme le cambodge, l’Inde ou le Sri Lanka, mais elle l'a fait dans un autre bout de l’Amazonie avec un voyage de presque 2 mois entre Brésil, Pérou, Bolivie et Chili.</p><p><br></p><p>Ce road road trip qui l'a marquée à tout jamais. Elle a accepté de partager une partie de ses péripéties. </p><p><br></p><p>Dans cet épisode je suis accompagné de mon ami Lova Jah lui aussi guyanais et qui a créé "l'hymne" de la Guyane, La Gwiyan Bel. <br><br>Découvrez <b>“AyDiYoSa : Talking To The People”</b>, le <b>podcast digital nomade</b> qui explore les mondes du voyage, de l’e-commerce, du digital et de la gastronomie à travers des discussions inspirantes et enrichissantes.</p><p><br></p><p>« Je suis un grand voyageur, dans ce podcast digital nomade, je partagerai les folles rencontres, la découverte de personnes aux parcours inspirants.</p><p>Le voyage est pour moi un parcours. 🚀 » Gérald JOSEPH-ALEXANDRE</p><br/><p>Hébergé par Ausha. Visitez <a href="https://ausha.co/politique-de-confidentialite">ausha.co/politique-de-confidentialite</a> pour plus d'informations.</p>]]></content:encoded>
                    <pubDate>Tue, 11 Feb 2025 06:00:00 +0000</pubDate>
                    <enclosure url="https://audio.ausha.co/JdK1mt3keYPd.mp3?t=1739063952" length="61155914" type="audio/mpeg"/>
                                        <link>https://podcast.ausha.co/aydiyosa-talking-to-the-people/taste-it-voyage-en-amazonie-avec-amanda-qui-vient-damazonie</link>
                    
                                    <itunes:author>Gérald JOSEPH-ALEXANDRE</itunes:author>
                    <itunes:explicit>false</itunes:explicit>
                                        <itunes:keywords>digital,food,travel,MARTINIQUE,Guadeloupe,e-commerce,amazonie,maurice,Guyane,french tech,réunion,aydiyosa,talking to the people</itunes:keywords>
                                    <itunes:duration>1:03:38</itunes:duration>
                    <itunes:episodeType>full</itunes:episodeType>
                                        <itunes:season>3</itunes:season>
                        <podcast:season>3</podcast:season>
                                                <itunes:episode>2</itunes:episode>
                            <podcast:episode>2</podcast:episode>
                                                        <itunes:subtitle>
    Finalement c'est le deuxième VRAI épisode du format TASTE IT dans lequel je vous fait voyager avec moi. 
    Lors de mon troisième voyage en Guyane française j'ai fait la connaissance d'Amanda comme elle dit elle même elle se sent profondément " D'Amazo...</itunes:subtitle>

                    
                    <googleplay:author>Gérald JOSEPH-ALEXANDRE</googleplay:author>
                                    <googleplay:explicit>false</googleplay:explicit>

                                        <itunes:image href="https://image.ausha.co/fz2K5TbaeJ4wJqIvp6beWCYUCLCzeeVHxILT43Jt_1400x1400.jpeg?t=1692870043"/>
                        <googleplay:image href="https://image.ausha.co/fz2K5TbaeJ4wJqIvp6beWCYUCLCzeeVHxILT43Jt_1400x1400.jpeg?t=1692870043"/>
                    
                                        <psc:chapters version="1.1">
                                                </psc:chapters>
                    
                                </item>
                        <item>
                    <title>DISCOVER - Les métiers de DSI &amp; Coach Agile avec Olivier CONQ</title>
                    <guid isPermaLink="false">b805673f40a7946524172afbbbe12d6ebd799007</guid>
                    <description><![CDATA[<p>Dans le format DISCOVER nous allons explorer au travers une mini-série les différents métiers du Digital.</p><p><br></p><p>Dans notre quotidien, le digital, l’informatique, les algorithmes sont omni présent. Et quand nous parlons algorithme bien souvent beaucoup de personnes pensent à l’intelligence artificielle.</p><p><br></p><p>Dans l'épisode d'aujourd'hui nous alons aborder plein de choses passionnantes. Car le profil et le parcours d'Olivier est exceptionnel. </p><p><br></p><p>A la fois DSI (Directeur Système d'Informations) mais également avec une immense philosophie Agile, son partage d'expérience est sans commune mesure.<br></p><p>Olivier CONQ a une longue carrière de DSI / Coach Agile / Manageur derrière lui, il a été entre autre été DSI de Rue du Commerce, de 24S du groupe LVMH ou de Quotatis du Groupe ADEO. </p><p><br></p><p>Aujourd’hui il est à la tête de plusieurs entreprises dont Action Agile pour laquelle il aura l’occasion de nous en dire plus tout à l’heure et est également consultant en tant que Manager de Transition chez Allianz.</p><p><br></p><p>Vous l’avez compris c’est assez difficile de résumer sa carrière et tout ce qu’il peut faire mais nous tenterons d’abord de vous expliquer ce qu’est le métier de DSI, comment l’agilité peut être introduite dans une DSI dans des projets.<br><br>« Je suis un grand voyageur, dans ce podcast digital nomade, je partagerai les folles rencontres, la découverte de personnes aux parcours inspirants.</p><p>Le voyage est pour moi un parcours. 🚀 » Gérald JOSEPH-ALEXANDRE</p><br/><p>Hébergé par Ausha. Visitez <a href="https://ausha.co/politique-de-confidentialite">ausha.co/politique-de-confidentialite</a> pour plus d'informations.</p>]]></description>
                    <content:encoded><![CDATA[<p>Dans le format DISCOVER nous allons explorer au travers une mini-série les différents métiers du Digital.</p><p><br></p><p>Dans notre quotidien, le digital, l’informatique, les algorithmes sont omni présent. Et quand nous parlons algorithme bien souvent beaucoup de personnes pensent à l’intelligence artificielle.</p><p><br></p><p>Dans l'épisode d'aujourd'hui nous alons aborder plein de choses passionnantes. Car le profil et le parcours d'Olivier est exceptionnel. </p><p><br></p><p>A la fois DSI (Directeur Système d'Informations) mais également avec une immense philosophie Agile, son partage d'expérience est sans commune mesure.<br></p><p>Olivier CONQ a une longue carrière de DSI / Coach Agile / Manageur derrière lui, il a été entre autre été DSI de Rue du Commerce, de 24S du groupe LVMH ou de Quotatis du Groupe ADEO. </p><p><br></p><p>Aujourd’hui il est à la tête de plusieurs entreprises dont Action Agile pour laquelle il aura l’occasion de nous en dire plus tout à l’heure et est également consultant en tant que Manager de Transition chez Allianz.</p><p><br></p><p>Vous l’avez compris c’est assez difficile de résumer sa carrière et tout ce qu’il peut faire mais nous tenterons d’abord de vous expliquer ce qu’est le métier de DSI, comment l’agilité peut être introduite dans une DSI dans des projets.<br><br>« Je suis un grand voyageur, dans ce podcast digital nomade, je partagerai les folles rencontres, la découverte de personnes aux parcours inspirants.</p><p>Le voyage est pour moi un parcours. 🚀 » Gérald JOSEPH-ALEXANDRE</p><br/><p>Hébergé par Ausha. Visitez <a href="https://ausha.co/politique-de-confidentialite">ausha.co/politique-de-confidentialite</a> pour plus d'informations.</p>]]></content:encoded>
                    <pubDate>Tue, 14 Jan 2025 06:00:00 +0000</pubDate>
                    <enclosure url="https://audio.ausha.co/2Vnext9p4l1P.mp3?t=1699216012" length="100577407" type="audio/mpeg"/>
                                        <link>https://podcast.ausha.co/aydiyosa-talking-to-the-people/discover-les-metiers-de-dsi-coach-agile-avec-olivier-conq</link>
                    
                                    <itunes:author>Gérald JOSEPH-ALEXANDRE</itunes:author>
                    <itunes:explicit>false</itunes:explicit>
                                        <itunes:keywords>digital,food,travel,MARTINIQUE,Guadeloupe,e-commerce,LVMH,agilité,maurice,Guyane,DSI,french tech,réunion,aydiyosa,talking to the people,24s,adeo,quotatis,rue du commerce,action agile</itunes:keywords>
                                    <itunes:duration>1:44:40</itunes:duration>
                    <itunes:episodeType>full</itunes:episodeType>
                                        <itunes:season>3</itunes:season>
                        <podcast:season>3</podcast:season>
                                                <itunes:episode>1</itunes:episode>
                            <podcast:episode>1</podcast:episode>
                                                        <itunes:subtitle>Dans le format DISCOVER nous allons explorer au travers une mini-série les différents métiers du Digital.Dans notre quotidien, le digital, l’informatique, les algorithmes sont omni présent. Et quand nous parlons algorithme bien souvent beaucoup de pers...</itunes:subtitle>

                    
                    <googleplay:author>Gérald JOSEPH-ALEXANDRE</googleplay:author>
                                    <googleplay:explicit>false</googleplay:explicit>

                                        <itunes:image href="https://image.ausha.co/fz2K5TbaeJ4wJqIvp6beWCYUCLCzeeVHxILT43Jt_1400x1400.jpeg?t=1692870043"/>
                        <googleplay:image href="https://image.ausha.co/fz2K5TbaeJ4wJqIvp6beWCYUCLCzeeVHxILT43Jt_1400x1400.jpeg?t=1692870043"/>
                    
                                        <psc:chapters version="1.1">
                                                </psc:chapters>
                    
                                </item>
                        <item>
                    <title>TASTE IT - Présentation de Moane MANGATTALE - Transat Québec Saint-Malo - Martinique Tchalian - Horizon</title>
                    <guid isPermaLink="false">71b5ba33af4ed61dde1d9f76b124aecd38d19d60</guid>
                    <description><![CDATA[<p>Oui c’est encore un hors série et je n’ai toujours pas repris le flux de production que j’avais promis mais ça viendra ! </p><p><br></p><p>Dans le dernier hors série je vous avez fais voyager avec l’équipage de Martinique horizon qui venait de réaliser la transat Jacques Vabres.</p><p><br></p><p>Là je suis à Québec City au Canada avec un nouvel équipier, un nouveau skipper qui rejoins l’aventure et je meurs d’envie de vous le présenter.</p><p><br></p><p>On a très peu de temps car le programme est très chargé alors en 20 minutes je vous promets de vous faire découvrir Moane MANGATALLE.</p><p><br></p><p>Il a 20 ans d’expériences de Tour Des Yoles Rondes de la Martinique a même pas 35 ans.</p><p><br></p><p>Et il en a gagné au moins une main.</p><p><br></p><p>Tous les martiniquais ont au moins vu son sourire une fois ainsi que son bras levé de la victoire.</p><p><br></p><p>Il a rejoint l’équipage de Martinique Horizon pour réaliser sa première course au large la Transat Québec Saint-Malo !</p><p><br></p><p>Je vous propose de découvrir Moane MANGATALLE quelques heures avant le grand départ !</p><br/><p>Hébergé par Ausha. Visitez <a href="https://ausha.co/politique-de-confidentialite">ausha.co/politique-de-confidentialite</a> pour plus d'informations.</p>]]></description>
                    <content:encoded><![CDATA[<p>Oui c’est encore un hors série et je n’ai toujours pas repris le flux de production que j’avais promis mais ça viendra ! </p><p><br></p><p>Dans le dernier hors série je vous avez fais voyager avec l’équipage de Martinique horizon qui venait de réaliser la transat Jacques Vabres.</p><p><br></p><p>Là je suis à Québec City au Canada avec un nouvel équipier, un nouveau skipper qui rejoins l’aventure et je meurs d’envie de vous le présenter.</p><p><br></p><p>On a très peu de temps car le programme est très chargé alors en 20 minutes je vous promets de vous faire découvrir Moane MANGATALLE.</p><p><br></p><p>Il a 20 ans d’expériences de Tour Des Yoles Rondes de la Martinique a même pas 35 ans.</p><p><br></p><p>Et il en a gagné au moins une main.</p><p><br></p><p>Tous les martiniquais ont au moins vu son sourire une fois ainsi que son bras levé de la victoire.</p><p><br></p><p>Il a rejoint l’équipage de Martinique Horizon pour réaliser sa première course au large la Transat Québec Saint-Malo !</p><p><br></p><p>Je vous propose de découvrir Moane MANGATALLE quelques heures avant le grand départ !</p><br/><p>Hébergé par Ausha. Visitez <a href="https://ausha.co/politique-de-confidentialite">ausha.co/politique-de-confidentialite</a> pour plus d'informations.</p>]]></content:encoded>
                    <pubDate>Sat, 13 Jul 2024 15:30:00 +0000</pubDate>
                    <enclosure url="https://audio.ausha.co/zYnG7sEQn65v.mp3?t=1720884108" length="21798287" type="audio/mpeg"/>
                                        <link>https://podcast.ausha.co/aydiyosa-talking-to-the-people/taste-it-presentation-de-moane-mangattale-transat-quebec-saint-malo-martinique-tchalian-horizon</link>
                    
                                    <itunes:author>Gérald JOSEPH-ALEXANDRE</itunes:author>
                    <itunes:explicit>false</itunes:explicit>
                                        <itunes:keywords>digital,food,travel,MARTINIQUE,Guadeloupe,e-commerce,maurice,Guyane,french tech,réunion,aydiyosa,talking to the people,martinique horizon,moane mangattale</itunes:keywords>
                                    <itunes:duration>22:42</itunes:duration>
                    <itunes:episodeType>full</itunes:episodeType>
                                        <itunes:season>2</itunes:season>
                        <podcast:season>2</podcast:season>
                                                <itunes:episode>3</itunes:episode>
                            <podcast:episode>3</podcast:episode>
                                                        <itunes:subtitle>Oui c’est encore un hors série et je n’ai toujours pas repris le flux de production que j’avais promis mais ça viendra ! Dans le dernier hors série je vous avez fais voyager avec l’équipage de Martinique horizon qui venait de réaliser la transat Jacque...</itunes:subtitle>

                    
                    <googleplay:author>Gérald JOSEPH-ALEXANDRE</googleplay:author>
                                    <googleplay:explicit>false</googleplay:explicit>

                                        <itunes:image href="https://image.ausha.co/fz2K5TbaeJ4wJqIvp6beWCYUCLCzeeVHxILT43Jt_1400x1400.jpeg?t=1692870043"/>
                        <googleplay:image href="https://image.ausha.co/fz2K5TbaeJ4wJqIvp6beWCYUCLCzeeVHxILT43Jt_1400x1400.jpeg?t=1692870043"/>
                    
                                        <psc:chapters version="1.1">
                                                </psc:chapters>
                    
                                </item>
                        <item>
                    <title>TASTE IT - Transat Jacques Vabre - Martinique Tchalian - Horizon - Le Debrief'</title>
                    <guid isPermaLink="false">e056ba071fa297d7acdb97d4d6de8c683e6a0cf0</guid>
                    <description><![CDATA[<p>Hervé JEAN-MARIE et Jean-Yves AGLAÉ sont deux amis de longue date. Avec deux autres potes Aymeric PINTO et Moane MANGATALLE, ils ont créé Martinique Horizon. Un projet 100% martiniquais pour prendre part à des compétitions de course au large. </p><p><br></p><p>En Octobre 2023, ils sont au départ de la Jacques Vabre, la mythique course transatlantique. C'est leur première, et pour cette première ils doivent rejoindre leur île mais depuis Le Havre.</p><p><br></p><p>Il s'en est passé des choses en 25 jours de navigation, ils ont accepté de revenir sur cette expérience incroyable pour nous livrer un épisode INÉDIT au coeur de la Jacques Vabre.</p><p><br></p><p>Très clairement dans cet épisode on va parler parcours, voyage, mer, transat, sport, inspiration, jeunesse, expériences ! </p><p><br></p><p>« Je suis un grand voyageur, dans ce podcast digital nomade, je partagerai les folles rencontres, la découverte de personnes aux parcours inspirants.</p><p>Le voyage est pour moi un parcours. 🚀 » Gérald JOSEPH-ALEXANDRE<br><br> Remerciements à </p><ul><li><p>Maxime SOREL</p></li><li><p>Simon JEAN-JOSEPH</p></li><li><p>Gilles LAMIRÉ</p></li><li><p>Ingrid LABEAU</p></li><li><p>Nathalie DO</p></li><li><p>E.sy Kennenga</p></li><li><p>Kéni PIPEROL</p></li><li><p>La famille TESNIERES </p></li><li><p>Jonathan </p></li><li><p>Luanne </p></li><li><p>Batilde</p></li></ul><br/><p>Hébergé par Ausha. Visitez <a href="https://ausha.co/politique-de-confidentialite">ausha.co/politique-de-confidentialite</a> pour plus d'informations.</p>]]></description>
                    <content:encoded><![CDATA[<p>Hervé JEAN-MARIE et Jean-Yves AGLAÉ sont deux amis de longue date. Avec deux autres potes Aymeric PINTO et Moane MANGATALLE, ils ont créé Martinique Horizon. Un projet 100% martiniquais pour prendre part à des compétitions de course au large. </p><p><br></p><p>En Octobre 2023, ils sont au départ de la Jacques Vabre, la mythique course transatlantique. C'est leur première, et pour cette première ils doivent rejoindre leur île mais depuis Le Havre.</p><p><br></p><p>Il s'en est passé des choses en 25 jours de navigation, ils ont accepté de revenir sur cette expérience incroyable pour nous livrer un épisode INÉDIT au coeur de la Jacques Vabre.</p><p><br></p><p>Très clairement dans cet épisode on va parler parcours, voyage, mer, transat, sport, inspiration, jeunesse, expériences ! </p><p><br></p><p>« Je suis un grand voyageur, dans ce podcast digital nomade, je partagerai les folles rencontres, la découverte de personnes aux parcours inspirants.</p><p>Le voyage est pour moi un parcours. 🚀 » Gérald JOSEPH-ALEXANDRE<br><br> Remerciements à </p><ul><li><p>Maxime SOREL</p></li><li><p>Simon JEAN-JOSEPH</p></li><li><p>Gilles LAMIRÉ</p></li><li><p>Ingrid LABEAU</p></li><li><p>Nathalie DO</p></li><li><p>E.sy Kennenga</p></li><li><p>Kéni PIPEROL</p></li><li><p>La famille TESNIERES </p></li><li><p>Jonathan </p></li><li><p>Luanne </p></li><li><p>Batilde</p></li></ul><br/><p>Hébergé par Ausha. Visitez <a href="https://ausha.co/politique-de-confidentialite">ausha.co/politique-de-confidentialite</a> pour plus d'informations.</p>]]></content:encoded>
                    <pubDate>Fri, 08 Dec 2023 06:00:00 +0000</pubDate>
                    <enclosure url="https://audio.ausha.co/019gpiPVqwOY.mp3?t=1701959305" length="144327200" type="audio/mpeg"/>
                                        <link>https://podcast.ausha.co/aydiyosa-talking-to-the-people/taste-it-transat-jacques-vabre-martinique-tchalian-horizon-le-debrief</link>
                    
                                    <itunes:author>Gérald JOSEPH-ALEXANDRE</itunes:author>
                    <itunes:explicit>false</itunes:explicit>
                                        <itunes:keywords>digital,food,travel,MARTINIQUE,Guadeloupe,e-commerce,maurice,Guyane,french tech,réunion,maxime sorel,Jacques Vabre,aydiyosa,talking to the people,tchalian</itunes:keywords>
                                    <itunes:duration>2:30:06</itunes:duration>
                    <itunes:episodeType>full</itunes:episodeType>
                                        <itunes:season>2</itunes:season>
                        <podcast:season>2</podcast:season>
                                                <itunes:episode>2</itunes:episode>
                            <podcast:episode>2</podcast:episode>
                                                        <itunes:subtitle>Hervé JEAN-MARIE et Jean-Yves AGLAÉ sont deux amis de longue date. Avec deux autres potes Aymeric PINTO et Moane MANGATALLE, ils ont créé Martinique Horizon. Un projet 100% martiniquais pour prendre part à des compétitions de course au large. En Octobr...</itunes:subtitle>

                    
                    <googleplay:author>Gérald JOSEPH-ALEXANDRE</googleplay:author>
                                    <googleplay:explicit>false</googleplay:explicit>

                                        <itunes:image href="https://image.ausha.co/fz2K5TbaeJ4wJqIvp6beWCYUCLCzeeVHxILT43Jt_1400x1400.jpeg?t=1692870043"/>
                        <googleplay:image href="https://image.ausha.co/fz2K5TbaeJ4wJqIvp6beWCYUCLCzeeVHxILT43Jt_1400x1400.jpeg?t=1692870043"/>
                    
                                        <psc:chapters version="1.1">
                                                </psc:chapters>
                    
                                </item>
                        <item>
                    <title>DISCOVER - Le métier de d'ingénieur en Intelligence Artificielle avec Anand BADRINATH</title>
                    <guid isPermaLink="false">a0d52965e45e5b1484f09515b49e85d9769e88c8</guid>
                    <description><![CDATA[<p>Dans le format DISCOVER nous allons explorer au travers une mini-série les différents métiers du Digital.<br></p><p>Dans notre quotidien, le digital, l’informatique, les algorithmes sont omni présent. Et quand nous parlons algorithme bien souvent beaucoup de personnes pensent à l’intelligence artificielle.</p><p><br></p><p>Dans l’épisode d’aujourd’hui, on va tenter d’ouvrir nos champs du possible. </p><p><br></p><p>D’essayer de comprendre un peu plus ce qu’est l’intelligence artificielle, de comment on interagit avec elle, de comment elle pourrait nous aider ? Et de se projeter sur quelques cas d’usage.</p><p>En gros l’IA qu’est-ce que c’est ? Quelles sont ses limites ? Doit-on avoir une éthique ?</p><p><br></p><p>Mon invité du jour est Anand BADRINATH ingénieur en intelligence artificielle spécialisé dans l’analyse d’image et aujourd’hui, coordinateur d’une équipe de création de solutions d’AI au sein de la startup Another Brain.</p><p><br></p><p>Pour le contacter <br><a href="https://www.linkedin.com/in/anand-badrinath/">https://www.linkedin.com/in/anand-badrinath/</a> </p><p><br></p><p>Le film dont l'on parle dans l'épisode est Ex Machina <a href="https://www.allocine.fr/film/fichefilm_gen_cfilm=219931.html">https://www.allocine.fr/film/fichefilm_gen_cfilm=219931.html</a> </p><p><br></p><p>N'hésitez pas à nous faire des retours en DM sur Instagram !</p><p><br></p><p>« Je suis un grand voyageur, dans ce podcast digital nomade, je partagerai les folles rencontres, la découverte de personnes aux parcours inspirants.</p><p>Le voyage est pour moi un parcours. 🚀 » Gérald JOSEPH-ALEXANDRE</p><br/><p>Hébergé par Ausha. Visitez <a href="https://ausha.co/politique-de-confidentialite">ausha.co/politique-de-confidentialite</a> pour plus d'informations.</p>]]></description>
                    <content:encoded><![CDATA[<p>Dans le format DISCOVER nous allons explorer au travers une mini-série les différents métiers du Digital.<br></p><p>Dans notre quotidien, le digital, l’informatique, les algorithmes sont omni présent. Et quand nous parlons algorithme bien souvent beaucoup de personnes pensent à l’intelligence artificielle.</p><p><br></p><p>Dans l’épisode d’aujourd’hui, on va tenter d’ouvrir nos champs du possible. </p><p><br></p><p>D’essayer de comprendre un peu plus ce qu’est l’intelligence artificielle, de comment on interagit avec elle, de comment elle pourrait nous aider ? Et de se projeter sur quelques cas d’usage.</p><p>En gros l’IA qu’est-ce que c’est ? Quelles sont ses limites ? Doit-on avoir une éthique ?</p><p><br></p><p>Mon invité du jour est Anand BADRINATH ingénieur en intelligence artificielle spécialisé dans l’analyse d’image et aujourd’hui, coordinateur d’une équipe de création de solutions d’AI au sein de la startup Another Brain.</p><p><br></p><p>Pour le contacter <br><a href="https://www.linkedin.com/in/anand-badrinath/">https://www.linkedin.com/in/anand-badrinath/</a> </p><p><br></p><p>Le film dont l'on parle dans l'épisode est Ex Machina <a href="https://www.allocine.fr/film/fichefilm_gen_cfilm=219931.html">https://www.allocine.fr/film/fichefilm_gen_cfilm=219931.html</a> </p><p><br></p><p>N'hésitez pas à nous faire des retours en DM sur Instagram !</p><p><br></p><p>« Je suis un grand voyageur, dans ce podcast digital nomade, je partagerai les folles rencontres, la découverte de personnes aux parcours inspirants.</p><p>Le voyage est pour moi un parcours. 🚀 » Gérald JOSEPH-ALEXANDRE</p><br/><p>Hébergé par Ausha. Visitez <a href="https://ausha.co/politique-de-confidentialite">ausha.co/politique-de-confidentialite</a> pour plus d'informations.</p>]]></content:encoded>
                    <pubDate>Tue, 05 Sep 2023 06:00:00 +0000</pubDate>
                    <enclosure url="https://audio.ausha.co/Zg3l7H1Q7ljv.mp3?t=1693828435" length="70689479" type="audio/mpeg"/>
                                        <link>https://podcast.ausha.co/aydiyosa-talking-to-the-people/discover-le-metier-de-d-ingenieur-en-intelligence-artificielle-avec-anand-badrinath</link>
                    
                                    <itunes:author>Gérald JOSEPH-ALEXANDRE</itunes:author>
                    <itunes:explicit>false</itunes:explicit>
                                        <itunes:keywords>IA,digital,food,travel,MARTINIQUE,Guadeloupe,e-commerce,maurice,Guyane,french tech,intelligence artificielle,réunion,aydiyosa,talking to the people</itunes:keywords>
                                    <itunes:duration>1:13:34</itunes:duration>
                    <itunes:episodeType>full</itunes:episodeType>
                                        <itunes:season>2</itunes:season>
                        <podcast:season>2</podcast:season>
                                                <itunes:episode>1</itunes:episode>
                            <podcast:episode>1</podcast:episode>
                                                        <itunes:subtitle>Dans le format DISCOVER nous allons explorer au travers une mini-série les différents métiers du Digital.Dans notre quotidien, le digital, l’informatique, les algorithmes sont omni présent. Et quand nous parlons algorithme bien souvent beaucoup de pers...</itunes:subtitle>

                    
                    <googleplay:author>Gérald JOSEPH-ALEXANDRE</googleplay:author>
                                    <googleplay:explicit>false</googleplay:explicit>

                                        <itunes:image href="https://image.ausha.co/fz2K5TbaeJ4wJqIvp6beWCYUCLCzeeVHxILT43Jt_1400x1400.jpeg?t=1692870043"/>
                        <googleplay:image href="https://image.ausha.co/fz2K5TbaeJ4wJqIvp6beWCYUCLCzeeVHxILT43Jt_1400x1400.jpeg?t=1692870043"/>
                    
                                        <psc:chapters version="1.1">
                                                </psc:chapters>
                    
                                </item>
                        <item>
                    <title>HORS SERIE -  Agilité : Comment convaincre une direction à passer à l'Agilité avec Olivier CONQ ?</title>
                    <guid isPermaLink="false">62607c7916011100125095ec</guid>
                    <description><![CDATA[<p>Pour ces premiers "HORS SERIE" nous allons aborder des questions fondamentales que se posent les organisations et le top management lorsqu'il s'agit de mettre en place d'agilité dans leur entreprise. </p><p><br></p><p>En partenariat avec Action Agile et Olivier CONQ, Manager de Transition IT et en Transformation Digitale qui possède plus de 15 ans d'expériences notamment en agilité nous tenterons de vous apporter des éléments de réponses.</p><p><br></p><p>Dans ce 4ème et dernier hors série Agile de la saison Olivier CONQ nous donne des arguments, quelques clés pour convaincre une direction de passer à l'agilité !</p><p><br></p><p>Vous pouvez retrouver toutes les vidéos d'Action Agile sur la chaine YouTube suivante : </p><p><br></p><p><a href="https://www.youtube.com/channel/UCv16JvDfjzsLt0fZBoY3vfg">https://www.youtube.com/channel/UCv16JvDfjzsLt0fZBoY3vfg</a></p><p><br></p><p>« Je suis un grand voyageur, dans ce podcast digital nomade, je partagerai les folles rencontres, la découverte de personnes aux parcours inspirants.</p><p>Le voyage est pour moi un parcours. 🚀 » Gérald JOSEPH-ALEXANDRE</p><p><br></p><p>N'hésitez pas à nous faire des retours en DM sur Instagram !</p><p><br></p><p>#Podcast #Digital #Antilles #AyDiYoSa #TalkingToThePeople #PodcastDigital #PodcastAntilles #AyDiYoSaPodcast #PodcastWeb #PodcastEcommerce #Martinique #Guadeloupe #PodcastMartinique #PodcastGuadeloupe #Web</p><p>#Discover #Aydiyosa #Experience #Ecommerce #Digital #Podcast #ActionAgile #HorsSerie #Agilite #Planification #ProjectManagement</p><p><br></p><p> Hébergé par Acast. Visitez <a href="https://acast.com/privacy">acast.com/privacy</a> pour plus d'informations.</p><br/><p>Hébergé par Ausha. Visitez <a href="https://ausha.co/politique-de-confidentialite">ausha.co/politique-de-confidentialite</a> pour plus d'informations.</p>]]></description>
                    <content:encoded><![CDATA[<p>Pour ces premiers "HORS SERIE" nous allons aborder des questions fondamentales que se posent les organisations et le top management lorsqu'il s'agit de mettre en place d'agilité dans leur entreprise. </p><p><br></p><p>En partenariat avec Action Agile et Olivier CONQ, Manager de Transition IT et en Transformation Digitale qui possède plus de 15 ans d'expériences notamment en agilité nous tenterons de vous apporter des éléments de réponses.</p><p><br></p><p>Dans ce 4ème et dernier hors série Agile de la saison Olivier CONQ nous donne des arguments, quelques clés pour convaincre une direction de passer à l'agilité !</p><p><br></p><p>Vous pouvez retrouver toutes les vidéos d'Action Agile sur la chaine YouTube suivante : </p><p><br></p><p><a href="https://www.youtube.com/channel/UCv16JvDfjzsLt0fZBoY3vfg">https://www.youtube.com/channel/UCv16JvDfjzsLt0fZBoY3vfg</a></p><p><br></p><p>« Je suis un grand voyageur, dans ce podcast digital nomade, je partagerai les folles rencontres, la découverte de personnes aux parcours inspirants.</p><p>Le voyage est pour moi un parcours. 🚀 » Gérald JOSEPH-ALEXANDRE</p><p><br></p><p>N'hésitez pas à nous faire des retours en DM sur Instagram !</p><p><br></p><p>#Podcast #Digital #Antilles #AyDiYoSa #TalkingToThePeople #PodcastDigital #PodcastAntilles #AyDiYoSaPodcast #PodcastWeb #PodcastEcommerce #Martinique #Guadeloupe #PodcastMartinique #PodcastGuadeloupe #Web</p><p>#Discover #Aydiyosa #Experience #Ecommerce #Digital #Podcast #ActionAgile #HorsSerie #Agilite #Planification #ProjectManagement</p><p><br></p><p> Hébergé par Acast. Visitez <a href="https://acast.com/privacy">acast.com/privacy</a> pour plus d'informations.</p><br/><p>Hébergé par Ausha. Visitez <a href="https://ausha.co/politique-de-confidentialite">ausha.co/politique-de-confidentialite</a> pour plus d'informations.</p>]]></content:encoded>
                    <pubDate>Wed, 01 Jun 2022 04:00:50 +0000</pubDate>
                    <enclosure url="https://audio.ausha.co/nrdVDsqlv05K.mp3?t=1693401145" length="8419681" type="audio/mpeg"/>
                                        <link>https://podcast.ausha.co/aydiyosa-talking-to-the-people/hors-serie-agilite-comment-convaincre-une-direction-a-passer-a-l-agilite-avec-olivier-conq</link>
                    
                                    <itunes:author>Gérald JOSEPH-ALEXANDRE</itunes:author>
                    <itunes:explicit>false</itunes:explicit>
                                        <itunes:keywords></itunes:keywords>
                                    <itunes:duration>08:46</itunes:duration>
                    <itunes:episodeType>full</itunes:episodeType>
                                    <itunes:subtitle>Pour ces premiers "HORS SERIE" nous allons aborder des questions fondamentales que se posent les organisations et le top management lorsqu'il s'agit de mettre en place d'agilité dans leur entreprise. En partenariat avec Action Agile et Olivier CONQ, Ma...</itunes:subtitle>

                    
                    <googleplay:author>Gérald JOSEPH-ALEXANDRE</googleplay:author>
                                    <googleplay:explicit>false</googleplay:explicit>

                                        <itunes:image href="https://image.ausha.co/rHKMnPqkbz12WlaWkIpKu0ygbCRGpzl1Ck8XiOmR_1400x1400.jpeg?t=1693401132"/>
                        <googleplay:image href="https://image.ausha.co/rHKMnPqkbz12WlaWkIpKu0ygbCRGpzl1Ck8XiOmR_1400x1400.jpeg?t=1693401132"/>
                    
                                        <psc:chapters version="1.1">
                                                </psc:chapters>
                    
                                </item>
                        <item>
                    <title>HORS SERIE -  Agilité : Quels sont les principaux freins de l'Agilité avec Olivier CONQ ?</title>
                    <guid isPermaLink="false">62607c468647800012289c9b</guid>
                    <description><![CDATA[<p>Pour ces premiers "HORS SERIE" nous allons aborder des questions fondamentales que se posent les organisations et le top management lorsqu'il s'agit de mettre en place d'agilité dans leur entreprise. </p><p><br></p><p>En partenariat avec Action Agile et Olivier CONQ, Manager de Transition IT et en Transformation Digitale qui possède plus de 15 ans d'expériences notamment en agilité nous tenterons de vous apporter des éléments de réponses.</p><p><br></p><p>Pour ce troisième épisode abordera l'épineuse questions des freins. Quels sont-ils ? Pourquoi une organisation peut être réticente ? Comment solutionner ce problème ? </p><p><br></p><p>La question est donc la suivante : </p><p><br></p><p>Quels sont les principaux freins de l'Agilité ?</p><p><br></p><p>Vous pouvez retrouver toutes les vidéos d'Action Agile sur la chaine YouTube suivante : </p><p><br></p><p><a href="https://www.youtube.com/channel/UCv16JvDfjzsLt0fZBoY3vfg">https://www.youtube.com/channel/UCv16JvDfjzsLt0fZBoY3vfg</a></p><p><br></p><p>« Je suis un grand voyageur, dans ce podcast digital nomade, je partagerai les folles rencontres, la découverte de personnes aux parcours inspirants.</p><p>Le voyage est pour moi un parcours. 🚀 » Gérald JOSEPH-ALEXANDRE</p><p><br></p><p>N'hésitez pas à nous faire des retours en DM sur Instagram !</p><p><br></p><p>#Podcast #Digital #Antilles #AyDiYoSa #TalkingToThePeople #PodcastDigital #PodcastAntilles #AyDiYoSaPodcast #PodcastWeb #PodcastEcommerce #Martinique #Guadeloupe #PodcastMartinique #PodcastGuadeloupe #Web</p><p>#Discover #Aydiyosa #Experience #Ecommerce #Digital #Podcast #ActionAgile #HorsSerie #Agilite #Planification #ProjectManagement</p><br/><p>Hébergé par Ausha. Visitez <a href="https://ausha.co/politique-de-confidentialite">ausha.co/politique-de-confidentialite</a> pour plus d'informations.</p>]]></description>
                    <content:encoded><![CDATA[<p>Pour ces premiers "HORS SERIE" nous allons aborder des questions fondamentales que se posent les organisations et le top management lorsqu'il s'agit de mettre en place d'agilité dans leur entreprise. </p><p><br></p><p>En partenariat avec Action Agile et Olivier CONQ, Manager de Transition IT et en Transformation Digitale qui possède plus de 15 ans d'expériences notamment en agilité nous tenterons de vous apporter des éléments de réponses.</p><p><br></p><p>Pour ce troisième épisode abordera l'épineuse questions des freins. Quels sont-ils ? Pourquoi une organisation peut être réticente ? Comment solutionner ce problème ? </p><p><br></p><p>La question est donc la suivante : </p><p><br></p><p>Quels sont les principaux freins de l'Agilité ?</p><p><br></p><p>Vous pouvez retrouver toutes les vidéos d'Action Agile sur la chaine YouTube suivante : </p><p><br></p><p><a href="https://www.youtube.com/channel/UCv16JvDfjzsLt0fZBoY3vfg">https://www.youtube.com/channel/UCv16JvDfjzsLt0fZBoY3vfg</a></p><p><br></p><p>« Je suis un grand voyageur, dans ce podcast digital nomade, je partagerai les folles rencontres, la découverte de personnes aux parcours inspirants.</p><p>Le voyage est pour moi un parcours. 🚀 » Gérald JOSEPH-ALEXANDRE</p><p><br></p><p>N'hésitez pas à nous faire des retours en DM sur Instagram !</p><p><br></p><p>#Podcast #Digital #Antilles #AyDiYoSa #TalkingToThePeople #PodcastDigital #PodcastAntilles #AyDiYoSaPodcast #PodcastWeb #PodcastEcommerce #Martinique #Guadeloupe #PodcastMartinique #PodcastGuadeloupe #Web</p><p>#Discover #Aydiyosa #Experience #Ecommerce #Digital #Podcast #ActionAgile #HorsSerie #Agilite #Planification #ProjectManagement</p><br/><p>Hébergé par Ausha. Visitez <a href="https://ausha.co/politique-de-confidentialite">ausha.co/politique-de-confidentialite</a> pour plus d'informations.</p>]]></content:encoded>
                    <pubDate>Tue, 17 May 2022 03:00:43 +0000</pubDate>
                    <enclosure url="https://audio.ausha.co/yErAvRsDQKLG.mp3?t=1693401152" length="10911962" type="audio/mpeg"/>
                                        <link>https://podcast.ausha.co/aydiyosa-talking-to-the-people/hors-serie-agilite-quels-sont-les-principaux-freins-de-l-agilite-avec-olivier-conq</link>
                    
                                    <itunes:author>Gérald JOSEPH-ALEXANDRE</itunes:author>
                    <itunes:explicit>false</itunes:explicit>
                                        <itunes:keywords></itunes:keywords>
                                    <itunes:duration>11:21</itunes:duration>
                    <itunes:episodeType>full</itunes:episodeType>
                                    <itunes:subtitle>Pour ces premiers "HORS SERIE" nous allons aborder des questions fondamentales que se posent les organisations et le top management lorsqu'il s'agit de mettre en place d'agilité dans leur entreprise. En partenariat avec Action Agile et Olivier CONQ, Ma...</itunes:subtitle>

                    
                    <googleplay:author>Gérald JOSEPH-ALEXANDRE</googleplay:author>
                                    <googleplay:explicit>false</googleplay:explicit>

                                        <itunes:image href="https://image.ausha.co/i0DoV06p0L9wTde75NmpQwxaAs1zmPIqarykSo6M_1400x1400.jpeg?t=1693401132"/>
                        <googleplay:image href="https://image.ausha.co/i0DoV06p0L9wTde75NmpQwxaAs1zmPIqarykSo6M_1400x1400.jpeg?t=1693401132"/>
                    
                                        <psc:chapters version="1.1">
                                                </psc:chapters>
                    
                                </item>
                        <item>
                    <title>HORS SERIE -  Agilité : Comment planifier en Agile avec Olivier CONQ ?</title>
                    <guid isPermaLink="false">62574362f25a120013105aac</guid>
                    <description><![CDATA[<p>Pour ces premiers "HORS SERIE" nous allons aborder des questions fondamentales que se posent les organisations et le top management lorsqu'il s'agit de mettre en place d'agilité dans leur entreprise. </p><p><br></p><p>En partenariat avec Action Agile et Olivier CONQ, Manager de Transition IT et en Transformation Digitale qui possède plus de 15 ans d'expériences notamment en agilité nous tenterons de vous apporter des éléments de réponses.</p><p><br></p><p>Pour ce second épisode nous aborderons la question de la plannification. Comment fournir un planning dans une organisation Agile ? Comment donner ou avoir de la visibilité sur les livrables qui seront fournis ? </p><p><br></p><p>La question est donc la suivante : </p><p><br></p><p>Comment planifier en Agile ?</p><p><br></p><p>Vous pouvez retrouver toutes les vidéos d'Action Agile sur la chaine YouTube suivante : </p><p><br></p><p><a href="https://www.youtube.com/channel/UCv16JvDfjzsLt0fZBoY3vfg">https://www.youtube.com/channel/UCv16JvDfjzsLt0fZBoY3vfg</a></p><p><br></p><p>« Je suis un grand voyageur, dans ce podcast digital nomade, je partagerai les folles rencontres, la découverte de personnes aux parcours inspirants.</p><p>Le voyage est pour moi un parcours. 🚀 » Gérald JOSEPH-ALEXANDRE</p><p><br></p><p>N'hésitez pas à nous faire des retours en DM sur Instagram !</p><p><br></p><p>#Podcast #Digital #Antilles #AyDiYoSa #TalkingToThePeople #PodcastDigital #PodcastAntilles #AyDiYoSaPodcast #PodcastWeb #PodcastEcommerce #Martinique #Guadeloupe #PodcastMartinique #PodcastGuadeloupe #Web</p><p>#Discover #Aydiyosa #Experience #Ecommerce #Digital #Podcast #ActionAgile #HorsSerie #Agilite #Planification #ProjectManagement</p><br/><p>Hébergé par Ausha. Visitez <a href="https://ausha.co/politique-de-confidentialite">ausha.co/politique-de-confidentialite</a> pour plus d'informations.</p>]]></description>
                    <content:encoded><![CDATA[<p>Pour ces premiers "HORS SERIE" nous allons aborder des questions fondamentales que se posent les organisations et le top management lorsqu'il s'agit de mettre en place d'agilité dans leur entreprise. </p><p><br></p><p>En partenariat avec Action Agile et Olivier CONQ, Manager de Transition IT et en Transformation Digitale qui possède plus de 15 ans d'expériences notamment en agilité nous tenterons de vous apporter des éléments de réponses.</p><p><br></p><p>Pour ce second épisode nous aborderons la question de la plannification. Comment fournir un planning dans une organisation Agile ? Comment donner ou avoir de la visibilité sur les livrables qui seront fournis ? </p><p><br></p><p>La question est donc la suivante : </p><p><br></p><p>Comment planifier en Agile ?</p><p><br></p><p>Vous pouvez retrouver toutes les vidéos d'Action Agile sur la chaine YouTube suivante : </p><p><br></p><p><a href="https://www.youtube.com/channel/UCv16JvDfjzsLt0fZBoY3vfg">https://www.youtube.com/channel/UCv16JvDfjzsLt0fZBoY3vfg</a></p><p><br></p><p>« Je suis un grand voyageur, dans ce podcast digital nomade, je partagerai les folles rencontres, la découverte de personnes aux parcours inspirants.</p><p>Le voyage est pour moi un parcours. 🚀 » Gérald JOSEPH-ALEXANDRE</p><p><br></p><p>N'hésitez pas à nous faire des retours en DM sur Instagram !</p><p><br></p><p>#Podcast #Digital #Antilles #AyDiYoSa #TalkingToThePeople #PodcastDigital #PodcastAntilles #AyDiYoSaPodcast #PodcastWeb #PodcastEcommerce #Martinique #Guadeloupe #PodcastMartinique #PodcastGuadeloupe #Web</p><p>#Discover #Aydiyosa #Experience #Ecommerce #Digital #Podcast #ActionAgile #HorsSerie #Agilite #Planification #ProjectManagement</p><br/><p>Hébergé par Ausha. Visitez <a href="https://ausha.co/politique-de-confidentialite">ausha.co/politique-de-confidentialite</a> pour plus d'informations.</p>]]></content:encoded>
                    <pubDate>Tue, 26 Apr 2022 03:00:17 +0000</pubDate>
                    <enclosure url="https://audio.ausha.co/M3Kp1tM8k901.mp3?t=1693401145" length="9859939" type="audio/mpeg"/>
                                        <link>https://podcast.ausha.co/aydiyosa-talking-to-the-people/hors-serie-agilite-comment-planifier-en-agile-avec-olivier-conq</link>
                    
                                    <itunes:author>Gérald JOSEPH-ALEXANDRE</itunes:author>
                    <itunes:explicit>false</itunes:explicit>
                                        <itunes:keywords></itunes:keywords>
                                    <itunes:duration>10:16</itunes:duration>
                    <itunes:episodeType>full</itunes:episodeType>
                                    <itunes:subtitle>Pour ces premiers "HORS SERIE" nous allons aborder des questions fondamentales que se posent les organisations et le top management lorsqu'il s'agit de mettre en place d'agilité dans leur entreprise. En partenariat avec Action Agile et Olivier CONQ, Ma...</itunes:subtitle>

                    
                    <googleplay:author>Gérald JOSEPH-ALEXANDRE</googleplay:author>
                                    <googleplay:explicit>false</googleplay:explicit>

                                        <itunes:image href="https://image.ausha.co/zMaAOD8UD9XGnUKnvxMiLbh3KXmPq8U2EJ455qF7_1400x1400.jpeg?t=1693401131"/>
                        <googleplay:image href="https://image.ausha.co/zMaAOD8UD9XGnUKnvxMiLbh3KXmPq8U2EJ455qF7_1400x1400.jpeg?t=1693401131"/>
                    
                                        <psc:chapters version="1.1">
                                                </psc:chapters>
                    
                                </item>
                        <item>
                    <title>HORS SERIE -  Agilité : Comment gérer un budget en Agile avec Olivier CONQ ?</title>
                    <guid isPermaLink="false">62557d7b72581200133f7b55</guid>
                    <description><![CDATA[<p>Pour ces premiers "HORS SERIE" nous allons aborder des questions fondamentales que se posent les organisations et le top management lorsqu'il s'agit de mettre en place d'agilité. </p><p><br></p><p>En partenariat avec Action Agile et Olivier CONQ, Manager de transition IT et en transformation digitale qui possède plus de 15 ans d'expériences notamment en agilité nous tenterons de vous apporter des éléments de réponses.</p><p><br></p><p>Dans ce premier episode la question est la suivante : </p><p><br></p><p>Comment gérer un budget en Agile ?</p><p><br></p><p>Une vidéo de cette capsule est aussi disponible ici </p><p><br></p><p>https://www.youtube.com/watch?v=CzjcphOU_3M</p><p><br></p><p>Vous pouvez retrouver toutes les vidéos d'Action Agile sur la chaine YouTube suivante : </p><p><br></p><p><a href="https://www.youtube.com/channel/UCv16JvDfjzsLt0fZBoY3vfg">https://www.youtube.com/channel/UCv16JvDfjzsLt0fZBoY3vfg</a></p><p><br></p><p>« Je suis un grand voyageur, dans ce podcast digital nomade, je partagerai les folles rencontres, la découverte de personnes aux parcours inspirants.</p><p>Le voyage est pour moi un parcours. 🚀 » Gérald JOSEPH-ALEXANDRE</p><p><br></p><p>N'hésitez pas à nous faire des retours en DM sur Instagram !</p><p><br></p><p>#Podcast #Digital #Antilles #AyDiYoSa #TalkingToThePeople #PodcastDigital #PodcastAntilles #AyDiYoSaPodcast #PodcastWeb #PodcastEcommerce #Martinique #Guadeloupe #PodcastMartinique #PodcastGuadeloupe #Web</p><p>#Discover #Aydiyosa #Experience #Ecommerce #Digital #Podcast #ActionAgile #HorsSerie #Agilite #Budget </p><p><br></p><br/><p>Hébergé par Ausha. Visitez <a href="https://ausha.co/politique-de-confidentialite">ausha.co/politique-de-confidentialite</a> pour plus d'informations.</p>]]></description>
                    <content:encoded><![CDATA[<p>Pour ces premiers "HORS SERIE" nous allons aborder des questions fondamentales que se posent les organisations et le top management lorsqu'il s'agit de mettre en place d'agilité. </p><p><br></p><p>En partenariat avec Action Agile et Olivier CONQ, Manager de transition IT et en transformation digitale qui possède plus de 15 ans d'expériences notamment en agilité nous tenterons de vous apporter des éléments de réponses.</p><p><br></p><p>Dans ce premier episode la question est la suivante : </p><p><br></p><p>Comment gérer un budget en Agile ?</p><p><br></p><p>Une vidéo de cette capsule est aussi disponible ici </p><p><br></p><p>https://www.youtube.com/watch?v=CzjcphOU_3M</p><p><br></p><p>Vous pouvez retrouver toutes les vidéos d'Action Agile sur la chaine YouTube suivante : </p><p><br></p><p><a href="https://www.youtube.com/channel/UCv16JvDfjzsLt0fZBoY3vfg">https://www.youtube.com/channel/UCv16JvDfjzsLt0fZBoY3vfg</a></p><p><br></p><p>« Je suis un grand voyageur, dans ce podcast digital nomade, je partagerai les folles rencontres, la découverte de personnes aux parcours inspirants.</p><p>Le voyage est pour moi un parcours. 🚀 » Gérald JOSEPH-ALEXANDRE</p><p><br></p><p>N'hésitez pas à nous faire des retours en DM sur Instagram !</p><p><br></p><p>#Podcast #Digital #Antilles #AyDiYoSa #TalkingToThePeople #PodcastDigital #PodcastAntilles #AyDiYoSaPodcast #PodcastWeb #PodcastEcommerce #Martinique #Guadeloupe #PodcastMartinique #PodcastGuadeloupe #Web</p><p>#Discover #Aydiyosa #Experience #Ecommerce #Digital #Podcast #ActionAgile #HorsSerie #Agilite #Budget </p><p><br></p><br/><p>Hébergé par Ausha. Visitez <a href="https://ausha.co/politique-de-confidentialite">ausha.co/politique-de-confidentialite</a> pour plus d'informations.</p>]]></content:encoded>
                    <pubDate>Wed, 13 Apr 2022 06:43:20 +0000</pubDate>
                    <enclosure url="https://audio.ausha.co/qrEDZs1g85Zk.mp3?t=1693401145" length="10979658" type="audio/mpeg"/>
                                        <link>https://podcast.ausha.co/aydiyosa-talking-to-the-people/hors-serie-agilite-comment-gerer-un-budget-en-agile-avec-olivier-conq</link>
                    
                                    <itunes:author>Gérald JOSEPH-ALEXANDRE</itunes:author>
                    <itunes:explicit>false</itunes:explicit>
                                        <itunes:keywords></itunes:keywords>
                                    <itunes:duration>11:26</itunes:duration>
                    <itunes:episodeType>full</itunes:episodeType>
                                    <itunes:subtitle>Pour ces premiers "HORS SERIE" nous allons aborder des questions fondamentales que se posent les organisations et le top management lorsqu'il s'agit de mettre en place d'agilité. En partenariat avec Action Agile et Olivier CONQ, Manager de transition I...</itunes:subtitle>

                    
                    <googleplay:author>Gérald JOSEPH-ALEXANDRE</googleplay:author>
                                    <googleplay:explicit>false</googleplay:explicit>

                                        <itunes:image href="https://image.ausha.co/faro6V5y7btbeMwGfzIEXEF4Y2GBLuM1Cfd5RLZt_1400x1400.jpeg?t=1693401131"/>
                        <googleplay:image href="https://image.ausha.co/faro6V5y7btbeMwGfzIEXEF4Y2GBLuM1Cfd5RLZt_1400x1400.jpeg?t=1693401131"/>
                    
                                        <psc:chapters version="1.1">
                                                </psc:chapters>
                    
                                </item>
                </channel>
    </rss>
""".trimIndent()

class PodcastFeedDetectorTest {

    lateinit var subject: PodcastFeedDetector

    @BeforeTest
    fun setUp() {
        subject = PodcastFeedDetector()
    }

    @Test
    fun testThatValidFeedIsValid() {
        val result = subject.isPodcastFeed(validFeed)
        Truth.assertThat(result).isTrue()
    }
}