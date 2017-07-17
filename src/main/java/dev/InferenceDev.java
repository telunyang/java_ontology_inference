/*
 * Apache Jena Download and Install
 * 1. https://jena.apache.org/download/index.cgi
 * 1.1 apache-jena – contains the APIs, SPARQL engine, the TDB native RDF database and command line tools
 * 1.2 apache-jena-fuseki – the Jena SPARQL server
 */

package dev;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.shared.WrappedIOException;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class InferenceDev {

	//命名空間
	private String NS = "http://localhost/by-jena.rdf#";
	
	//檔案路徑
	private String path, path_rdf, path_rule, path_rdf_inf, path_owl;
	
	//建立 Ontology
	protected OntModel model = ModelFactory.createOntologyModel();
	
	//靜態資料庫連線
	static Connection connection;
	
	protected JsonObject json = new JsonObject();
	
	public static void main(String[] args) {
		try
		{
			InferenceDev mycode = new InferenceDev();
			
			//資料庫連線
			connection = DriverManager.getConnection("jdbc:mariadb://localhost:3306/health?user=root&password=root");
			
			//mycode.initOWL();
			mycode.initRDF(0);
//			if(mycode.verify())
//			{
//				mycode.reason();
//			}
			
			connection.close();
			
		}
		catch(WrappedIOException e)
		{
			System.out.println( e.getMessage() );
		}
		catch(Exception e)
		{
			System.out.println( e.getMessage() );
		}
		
	}
	
	public InferenceDev()
	{
		this.path = "/home/darren/workspace/inference-dev/files/";
		
		this.path_rdf = this.path + "by-jena.rdf";
		this.path_rule = this.path + "rule.txt";
		this.path_rdf_inf = this.path + "by-jena-result.rdf";
		this.path_owl = this.path + "pizza.owl";
	}
	
	protected void initOWL()
	{
		try
		{
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			
			File file = new File(this.path_owl);
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
			
			System.out.println("Number of axioms: " + ontology.getAxiomCount());
	        //manager.saveOntology(ontology, IRI.create(file.toURI()));
			
		}
		catch(Exception e)
		{
			System.out.println( e.getMessage() );
		}
	}
	
	protected void initRDF(Integer id)
	{
		try
		{
			//查詢用 SQL 語法
			String sql_select_class = "";
			sql_select_class += "select `id`, `class_name`, `parent_id` ";
			sql_select_class += "from `class` ";
			sql_select_class += "where `parent_id` = ? ";
			
			//查詢 class 階層
			PreparedStatement ps = connection.prepareStatement(sql_select_class);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			
			while(rs.next())
			{
				//System.out.println( rs.getInt("id") + ", " + rs.getString("class_name") + ", " + rs.getInt("parent_id") );
				
				//OntClass obj = this.model.createClass(this.NS + rs.getString("class_name"));
				//obj_class = this.model.createClass(this.NS + class_name);
				
				this.json.put("id", rs.getInt("id"));
				this.json.put("class_name", rs.getString("class_name"));
				this.json.put("parent_id", rs.getInt("parent_id"));
				this.json.put("child", "");
				
				JsonObject json_child = new JsonObject();
				json_child.put("id", 2);
				json_child.put("class_name", "Animal");
				json_child.put("parent_id", 1);
				json_child.put("child", "");
				
				this.json.put("child", json_child);
				
				System.out.println(this.json);
				
				//this.initRDF(rs.getInt("id"));
				
			}
		}
		catch(SQLException e)
		{
			System.out.println("SQLException: ");
			System.out.println( e.getMessage() );
		}
		catch(Exception e)
		{
			System.out.println("Exception: ");
			System.out.println( e.getMessage() );
		}
	}
	
	private void addSubClass()
	{
		try
		{
			
		}
		catch(Exception e)
		{
			System.out.println("Exception: ");
			System.out.println( e.getMessage() );
		}
	}
	
	/**
	 * 測試透過 Jena 建立 RDF
	 */
	protected void init()
	{
		try
		{
			//新增本體論 model
			OntModel model = ModelFactory.createOntologyModel();
			
			//新增類別 Animal
			OntClass Animal = model.createClass(this.NS + "Animal");
	
			//新增 Animal 子類別 Carnivore
			OntClass Carnivore = model.createClass(this.NS + "Carnivore");
			Animal.addSubClass(Carnivore);
			
			//新增 Animal 子類別 Herbivore
			OntClass Herbivore = model.createClass(this.NS + "Herbivore");
			Animal.addSubClass(Herbivore);
			
			//替 Animal 增加 name 屬性
			DatatypeProperty name = model.createDatatypeProperty(this.NS + "name");
			name.addDomain(Animal);
			name.addRange(XSD.xstring);
			
			//新增 Herbivore 的實體 Cow
			Individual Cow = Herbivore.createIndividual(this.NS + "Cow");
			
			//為 Cow 的屬性 name 新增為 Bob
			Cow.addProperty(name, this.NS + "Bob");
			
			//將 Cow 的 RDF Type 設定為 Herbivore 
			//Cow.addRDFType(Herbivore);
			Cow.addProperty(RDF.type, this.NS + "Herbivore");
			
			//新增 Carnivore 的實體 Tiger
			Individual Tiger = Carnivore.createIndividual(this.NS + "Tiger");
			
			//將 Tiger 的 RDF Type 設定為 Carnivore
			//Tiger.addRDFType(Carnivore);
			Tiger.addProperty(RDF.type, this.NS + "Carnivore");
			
			//新增類別 Plant
			OntClass Plant = model.createClass(this.NS + "Plant");
			
			//新增 Grass 實體
			Individual Grass = Plant.createIndividual(this.NS + "Grass");
			
			//將 Grass 的 RDF Type 設定為 Plant
			//Grass.addRDFType(Plant);
			Grass.addProperty(RDF.type, this.NS + "Plant");
			
			//新增 eat 屬性
			ObjectProperty eat = model.createObjectProperty(this.NS + "eat");
			eat.addDomain(Animal); //誰 eat
			eat.addRange(Plant); //eat 誰
			
			//為 Cow 的屬性 eat 對象設定為 Grass
			Cow.addProperty(eat, Grass); 
			
			//將 mode 儲存為 RDF 格式的檔案
			this.saveFile(model);
		}
		catch(Exception e)
		{
			System.out.println( e.getMessage() );
		}
	}
	
	//儲存檔案
	private void saveFile(OntModel model)
	{
		//Reference: https://jena.apache.org/documentation/io/rdf-input.html
		//Reference: https://jena.apache.org/documentation/io/rdf-output.html
		FileOutputStream out = null;
		try 
		{
			out = new FileOutputStream(this.path_rdf);
			//model.write(out, "RDF/XML-ABBREV"); //"RDF/XML-ABBREV"為儲存的格式
			model.write(out, "RDF/XML");
		} 
		catch (IOException ignore) 
		{
			ignore.printStackTrace();
		}
	}
	
	//驗證推論 Model 的有效性
	protected boolean verify()
	{
		boolean bool = false;
		try
		{
			Model m = FileManager.get().loadModel(this.path_rdf);
			InfModel infmodel = ModelFactory.createRDFSModel(m);
			ValidityReport validity = infmodel.validate();
			if (validity.isValid()) 
			{
			    System.out.println("Validity is OK");
			    bool = true;
			} 
			else 
			{
			    System.out.println("Conflicts");
			    for (Iterator i = validity.getReports(); i.hasNext(); ) 
			    {
			        System.out.println(" - " + i.next());
			    }
			}
		}
		catch(WrappedIOException e)
		{
			System.out.println( e.getMessage() );
		}
		catch(Exception e)
		{
			System.out.println( e.getMessage() );
		}
		return bool;
	}
	
	//進行推論
	protected void reason()
	{
		try
		{
			//讀取檔案
			Model model = ModelFactory.createDefaultModel();
			model.read( this.path_rdf );
			
			//建立推論器 (來自 rule file)
			Reasoner reasoner = new GenericRuleReasoner( Rule.rulesFromURL( this.path_rule ) );
			
			//產生推論後的 Ontology Model
			InfModel infModel = ModelFactory.createInfModel( reasoner, model );

			//輸出在推論後 Ontology Model 的各種 statements
			StmtIterator it = infModel.listStatements();
			
			//如果有 statement 就迭代輸出
			while ( it.hasNext() )
			{
				//建立 statement 物件
				Statement stmt = it.nextStatement();
				
				Resource subject = stmt.getSubject(); //推論的主體
				Property predicate = stmt.getPredicate(); //斷言
				RDFNode object = stmt.getObject(); //推論的客體
				
				//用正規表達式過濾字串，縮短檢視範圍
				//Pattern pattern = Pattern.compile("http:\\/\\/localhost\\/");
				Pattern pattern = Pattern.compile("hunts");
				
				//取得過濾後的媒合群組
				Matcher matcher = pattern.matcher(predicate.toString());
				
				//有媒合結果，就輸出
				if(matcher.find())
				{
					System.out.println( "[" + subject.toString() + "] => [" + predicate.toString() + "] => [ " + object.toString() + "]" );
				}
				
				//System.out.println( subject.toString() + " " + predicate.toString() + " " + object.toString() );
			}
			
			//將推論後的 Ontology Model 存檔
			FileOutputStream out = null;
			try 
			{
				out = new FileOutputStream(this.path_rdf_inf);
				infModel.write(out, "RDF/XML-ABBREV"); //"RDF/XML-ABBREV"為儲存的格式
				//model.write(out, "RDF/XML");
			} 
			catch (IOException ignore) 
			{
				ignore.printStackTrace();
			}
			
			//用 SPARQ 查詢「推論後」的 Ontology Model
			String queryString = "PREFIX NS:  <" + this.NS + ">"
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
					+ "SELECT ?c " 
					+ "WHERE{ ?c NS:hunts ?h . ?h rdf:type NS:Herbivore   }";
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.create(query, infModel);
			
			org.apache.jena.query.ResultSet results = qexec.execSelect();
			
			//輸出 SPARQ 查詢後的結果
			ResultSetFormatter.outputAsJSON(System.out, results);

		}
		catch(WrappedIOException e)
		{
			System.out.println( e.getMessage() );
		}
		catch(Exception e)
		{
			System.out.println( e.getMessage() );
		}
	}

}

