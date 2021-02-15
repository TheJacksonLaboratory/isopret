<!doctype html>
<html class="no-js" lang="">

<head>
  <meta charset="utf-8">
  <meta http-equiv="x-ua-compatible" content="ie=edge">
  <title>Isopret: Isoform interpretation</title>
  <meta name="description" content="">
  <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

  <style>

* {
    -moz-box-sizing: border-box;
    -webkit-box-sizing: border-box;
    box-sizing: border-box
}

html, body, h1, li, a, article, aside, footer, header, main, nav, section {
	padding: 0;
	margin: 0;
}

html, body {
	font-size:14px;
}

body {
	font-family:"DIN Next", Helvetica, Arial, sans-serif;
	line-height:1.25;
	background-color:#EEFBFB;
}


body > header, nav, main, body > section, footer {
max-width:1200px;
margin-left:auto;
margin-right:auto;
}

@media(min-width:1440px) {
body > header, nav, main, body > section, footer {
    width:90%;
    max-width:unset;
    }
}

main, body > section {
	margin-top:1.5rem;
	margin-bottom:1.5rem;
}

body > header, body > section {
	padding:2.1rem 2rem 1.6rem;
}

.fr {
  float: right;
}

a[href] {
	color:#05396b;
}

a[href]:hover {
	color:#009ed0;
}

p {
	padding:0;
	margin:0.75rem 0;
}

h1 {
	font-family:"DIN Next", Helvetica, Arial, sans-serif;
	font-weight:700;
	font-size:1.8rem;
	line-height:1;
}

.center {
  text-align: center;
}

main > section > a[name="othergenes"] > h3,
h2 {
	font-family:"DIN Next", Helvetica, Arial, sans-serif;
	font-weight:700;
	font-size:1.5rem;
	line-height:1;
	margin:0 0 0.5rem;
	padding:0;
}

h3 {
	font-family:"DIN Next", Helvetica, Arial, sans-serif;
	font-weight:700;
	font-size:1.2rem;
	line-height:1;
	margin:0 0 0.5rem;
	padding:0;
}



main ul, main ol {
	margin:0.5rem 0 0.5rem 1.4rem;
	padding:0;
}

main li {
	margin:0.25rem 0;
	padding:0;
}

.banner {
	background-color: #05396b;
	color: white;
}

nav {
	background-color: #4DA8DA;
	margin-top:1px;
	overflow:auto;
	zoom:1;
	padding:0;
}

nav a[href] {
	color:white;
	text-decoration:none;
	color:rgba(255,255,255,0.8);
	font-size:1.2rem;
	display:block;
	padding:1rem;
	font-weight:400;
}

nav li:last-child a[href] {
	padding-right:2.25rem;
}

nav a[href]:hover {
	color:#05396b;
	background-color:#04c3ff;
}

#navi ul {
	display:table;
	float:right;
	margin:0;
}

#navi li {
	display:block;
	float:left;
}

main > section:first-child {
	margin-top:1.5rem;
	margin-bottom:1.5rem;
	background-color:white;
	padding:2.1rem 2rem 1.6rem;
}

main > section {
	margin-top:1.5rem;
	margin-bottom:0;
	background-color:white;
	padding: .5rem;
}

main > section > article {
	padding: 1.5rem;
	margin-top:1px;
	background-color:white;
}

table {
	border-collapse: collapse;
	width:100%;
	margin:0.5rem 0;
}

th, td {
	text-align:left;
	padding:0.4rem 0.5rem 0.25rem;
}

th {
	background-color: #e0e3ea;
	border-bottom:1px solid white;
}

table.goTable {
	width:auto;
	min-width:50%;
}

table.goTable td {
	background-color:#f0f3fa;
}

table.posttest {
	width:auto;
	min-width:50%;
	margin-left:auto;
    margin-right:auto;
    border: 1px solid black;
}

table.posttest td {
    line-height: 40px;
}

table.posttest th  {font-size:1.5rem;}

table.posttest tr:nth-child(even) {background: #F5F5F5}
table.posttest tr:nth-child(odd) {background: #FFF}
td.posttest {font-size:1.3rem;}

table.minimalistBlack th,
table.minimalistBlack td {
	border:2px solid #e0e3ea;
}

table.minimalistBlack.red td {
	background: red;
}

td.red {
	background-color:#f0f3fa;
}


a[name="othergenes"] table.goTable {

}

a[name="othergenes"] table.goTable td.disease {
	font-size:0.928rem;
	padding-top:0.35rem;
	padding-bottom:0.15rem;
	text-transform: lowercase
}

a[name="othergenes"] table.goTable > tbody > tr:nth-child(even) > td {
	background-color:white;
}

a[name="othergenes"] table.goTable > tbody > tr:hover > td {
	background-color:#cceaff;
}

a[name="othergenes"] table.goTable a {
	text-decoration: none;
	display:block;
}

a[name="othergenes"] table.goTable a:hover {
	text-decoration: underline;
}

a[name="othergenes"] table.goTable a::first-letter {
	text-transform: uppercase;
}

/* Create three equal columns that floats next to each other */
.column {
  float: left;
  width: 33%;
  padding: 10px;
}


.generow{
    width: 90%;
    min-height: 100px;
    margin: 0 auto;
    display: -webkit-flex; /* Safari */
    display: flex; /* Standard syntax */
}

.generow .column{
    padding: 10px;
    background: #dbdfe5;
    -webkit-flex: 1; /* Safari */
    -ms-flex: 1; /* IE 10 */
    flex: 1; /* Standard syntax */
}
.generow .column.bg-alt{
    background: #b4bac0;
}

.svgrow{
    width: 90%;
    min-height: 100px;
    margin: 0 auto;
    display: -webkit-flex; /* Safari */
    display: flex; /* Standard syntax */
}

footer {
	background-color: #05396b;
	color: white;
	padding: 1rem 2rem;
}

/* The following links are in the SVG for the differentials */
a.svg:link, a.svg:visited {
  cursor: pointer;
}

a.svg text,
text a.svg {
  fill: blue; /* Even for text, SVG uses fill over color */
  text-decoration: underline;
}

a.svg:hover, a.svg:active {
  outline: dotted 1px blue;
}

.features-title {
  background-color: #05396b;
  color: white;
}

.features-title:nth-child(1) {
  border-right: 2px solid white;
}

.features-data {
  background-color: #e0e3ea;
}

.features-data:nth-child(1) {
  border-right: 2px solid white;
}
.no-list-style {
  list-style-type: none;
}

.tooltip {
  position: relative;
  display: inline-block;
  border-bottom: 1px dotted black; /* If you want dots under the hoverable text */
  outline: none;
  width: 150px;
  min-width: 150px;
  max-width: 250px;
}

/* Tooltip text */
.tooltip .tooltiptext {
  visibility: hidden;
  background-color: black;
  color: #fff;
  text-align: left;
  padding: 5px 0;
  border-radius: 6px;
  /* Position the tooltip text - see examples below! */
  position: absolute;
  z-index: 1;
}

/* Show the tooltip text when you mouse over the tooltip container */
.tooltip:hover .tooltiptext {
  visibility: visible;
}

.table-btn {
    display: block;
    font-weight: bold;
    padding: 10px;
    background-color: #05396b;
    width: fit-content;
    color: white;
    cursor: pointer;
}


#hide-dasgo-table, #dasgo-table {
  display: none;
}

#hide-dgego-table, #dgego-table {
  display: none;
}

#hide-dasdgego-table, #dasdgego-table {
  display: none;
}

.badge{
  font:  Arial, Helvetica, sans-serif;
  font-size: x-large;
  font-weight: bolder;
  line-height: 1;
  color: #f4f4f4;
  background-color: #214fe0;
  padding:0.25rem;
}



</style>
</head>

<body>
  <!--[if lte IE 9]>
    <p class="browserupgrade">You are using an <strong>outdated</strong> browser. Please <a href="https://browsehappy.com/">upgrade your browser</a> to improve your experience and security.</p>
  <![endif]-->
<header class="banner">
    <h1><font color="#FFDA1A">Isopret</font></h1>
</header>

<nav>
  <div id="navi">
    <ul>
      <li><a href="#sample">Sample</a></li>
      <li><a href="#go">GO analysis</a></li>
      <li><a href="#dgedas">DGE/DAS</a></li>
      <li><a href="#about">About</a></li>
    </ul>
  </div>
</nav>

<main>
  <section>
    <a name="sample"></a>
    <article>
      <h2>Isopret &mdash; Isoform interpretation.</h2>
      <p>HBA-DEALS analysis file: ${hbadealsFile}.</p>
      <p>${n_population} genes were included in the analysis. ${n_das} had one or more differentially spliced isoforms.
            ${n_dge} were differentially expressed.</p>
      <p>Using a desired FDR threshold of ${probability_threshold}, a probability threshold of
          ${expression_threshold} was calculated for differential expression, and a threshold of
          ${splicing_threshold} was calculated for differential splicing.</p>
    </article>
  </section>

  <section>
    <a name="go"></a>
    <article>
      <h2>Gene Ontology Overrepresentation Analysis.</h2>
      <p>Gene Ontology <tt>go.obo</tt> file: ${go_version} with ${n_go_terms} terms.</p>
      <p>${annotation_term_count} terms used for a total of ${annotation_count} annotations for ${annotated_genes} genes.</p>
      <h4>Differentially expressed genes (n=${n_dge})</h4>
       <p>${n_dge_unmapped} of the ${n_dge} genes could not be mapped to gene ids for GO analysis
      <a id="show-dgego-table" class="table-btn" onclick="showDgeGoTable()">Show Table</a>
      <a id="hide-dgego-table" class="table-btn" onclick="hideDgeGoTable()">Hide Table</a>
      ${dgeTable}
      <h4>Differentially spliced genes (n=${n_das?string})</h4>
      <p>${n_das_unmapped} of the ${n_das} genes could not be mapped to gene ids for GO analysis.</p>
      <a id="show-dasgo-table" class="table-btn" onclick="showDasGoTable()">Show Table</a>
      <a id="hide-dasgo-table" class="table-btn" onclick="hideDasGoTable()">Hide Table</a>
      ${dasTable}
       <p>A total of ${n_das} out of ${n_population} genes in the population showed differential alternative splicing (DAS).</p>
    </article>
  </section>

 <a name="dgedas"></a>
 <#list genelist as gene>
  <section>
    ${gene}
  </section>
  </#list>


  <section>
    <a name="about"></a>
    <article>
      <h2>About</h2>
      <p>Isopret TODO some text.</p>
    </article>
  </section>

  <span id="tooltip" display="none" style="position: absolute; display: none;"></span>
</main>
<footer>
  <p>Isopret &copy; 2021</p>
</footer>

<script>
  function showTooltip(evt, text) {
    let tooltip = document.getElementById("tooltip");
    tooltip.innerText = text;
    tooltip.style.display = "block";
    tooltip.style.left = evt.pageX + 10 + 'px';
    tooltip.style.top = evt.pageY + 10 + 'px';
  }

  function hideTooltip() {
    var tooltip = document.getElementById("tooltip");
    tooltip.style.display = "none";
  }


  function showDgeGoTable() {
    var table = document.getElementById("dgego-table");
    table.style.display = "block";
    var showtablebtn = document.getElementById("show-dgego-table");
    showtablebtn.style.display = "none";
    var hidetablebtn = document.getElementById("hide-dgego-table");
    hidetablebtn.style.display = "block";
  }

  function hideDgeGoTable() {
    var table = document.getElementById("dgego-table");
    table.style.display = "none";
    var showtablebtn = document.getElementById("show-dgego-table");
    showtablebtn.style.display = "block";
    var hidetablebtn = document.getElementById("hide-dgego-table");
    hidetablebtn.style.display = "none";
  }

  function showDasGoTable() {
    var table = document.getElementById("dasgo-table");
    table.style.display = "block";
    var showtablebtn = document.getElementById("show-dasgo-table");
    showtablebtn.style.display = "none";
    var hidetablebtn = document.getElementById("hide-dasgo-table");
    hidetablebtn.style.display = "block";
  }

  function hideDasGoTable() {
    var table = document.getElementById("dasgo-table");
    table.style.display = "none";
    var showtablebtn = document.getElementById("show-dasgo-table");
    showtablebtn.style.display = "block";
    var hidetablebtn = document.getElementById("hide-dasgo-table");
    hidetablebtn.style.display = "none";
  }
</script>
</body>
</html>
