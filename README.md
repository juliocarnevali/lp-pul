<h1> A Graph-based Approach for Positive and Unlabeled Learning</h1>

<h3> Julio C. Carnevali, Rafael G. Rossi, Evangelos Milios, Alneu A. Lopes</h3>

<strong>Abstract</strong> Positive and Unlabeled Learning (PUL) uses unlabeled documents and a few positive documents, in which the user is interested, for retrieving a set of "interest" documents from a text collection. Usually, PUL approaches are based on the vector space model. However, when dealing with semi-supervised learning for text classification or information retrieval, graph-based approaches have been proved to outperform vector space model-based approaches. So in this article a graph-based approach for PUL is proposed, referred to as Label Propagation for Positive and Unlabeled Learning (LP-PUL). The proposed framework consists of three steps: (i) building a similarity graph, (ii) identifying reliable negative documents, and (iii) performing label propagation to classify the remaining unlabeled documents as positive or negative. We carried out evaluation to measure the impact of the different choices in each step of the proposed framework. We also demonstrated that the proposal surpasses the classification performance of other PUL algorithms.

<strong>Contacts</strong> {carnevali.julio,rgr.rossi,evangelos.milios}@gmail.com, alneu@icmc.usp.br

<h3>Source code and complete results</h3>
Source code and complete results are available in the <a href="https://github.com/juliocarnevali/lp-pul">GitHub repository</a>.

<h3>Text collections</h3>
Text collections used in this project are available in:
Rossi, Rafael & Marcacini, Ricardo & Rezende, Solange. (2013). Benchmarking Text Collections for Classification and Clustering Tasks. Technical Report 395, Institute of Mathematics and Computer Sciences - University of Sao Paulo.
