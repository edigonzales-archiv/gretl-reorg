package ch.so.agi.gretl.jobs;

import ch.interlis.ioxwkf.shp.ShapeReader;
import ch.so.agi.gretl.util.TestUtilSqlPg;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.*;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;

public class ShpExportTest {
    @Test
    public void exportOk() throws Exception {
        String schemaName = "shpexport".toLowerCase();
        Connection con = null;
        try {
            // prepare postgres
            con = TestUtilSqlPg.connect();
            TestUtilSqlPg.createOrReplaceSchema(con, schemaName);
            Statement s1 = con.createStatement();
            s1.execute("CREATE TABLE "+schemaName+".exportdata(t_id serial, \"Aint\" integer, adec decimal(7,1), atext varchar(40), aenum varchar(120),adate date, atimestamp timestamp, aboolean boolean,geom_so geometry(POINT,2056))");
            s1.execute("INSERT INTO "+schemaName+".exportdata(t_id, \"Aint\", adec, atext, adate, atimestamp, aboolean,geom_so) VALUES (1,2,3.4,'abc','2013-10-21','2015-02-16T08:35:45.000','true',ST_GeomFromText('POINT(2638000.0 1175250.0)',2056))");
            s1.execute("INSERT INTO "+schemaName+".exportdata(t_id) VALUES (2)");
            s1.close();
            TestUtilSqlPg.grantDataModsInSchemaToUser(con, schemaName, TestUtilSqlPg.CON_DMLUSER);

            con.commit();
            TestUtilSqlPg.closeCon(con);

            // run job
            BuildResult result = GradleRunner.create()
                    .withProjectDir(new File("src/functionalTest/jobs/ShpExport/"))
                    .withArguments("-i")
                    .withPluginClasspath()
                    .build();

            // check results
            assertEquals(SUCCESS, result.task(":shpexport").getOutcome());

            {
                System.out.println("cwd "+new File(".").getAbsolutePath());
                // Open the file for reading
                FileDataStore dataStore = FileDataStoreFinder.getDataStore(new File("src/functionalTest/jobs/ShpExport/data.shp"));
                SimpleFeatureSource featuresSource = dataStore.getFeatureSource();
                SimpleFeatureIterator featureCollectionIter=featuresSource.getFeatures().features();
                // feature object
                {
                    SimpleFeature shapeObj=(SimpleFeature) featureCollectionIter.next();
                    Object attr1=shapeObj.getAttribute("Aint");
                    assertEquals(2,attr1);
                    Object attr2=shapeObj.getAttribute("atext");
                    assertEquals("abc",attr2);
                    Object attr3=shapeObj.getAttribute("adec");
                    assertEquals(3.4,attr3);
                    Object attr4=shapeObj.getAttribute(ShapeReader.GEOTOOLS_THE_GEOM);
                    assertEquals(new Coordinate(2638000.0,1175250.0),((Point)attr4).getCoordinate());
                    Object attr5=shapeObj.getAttribute("adate");
                    assertEquals(new java.util.Date(2013-1900,10-1,21),attr5);
                    Object attr6=shapeObj.getAttribute("aboolean");
                    assertEquals(String.class.getName(),attr6.getClass().getName());
                    assertEquals("true",attr6);
                    Object attr7=shapeObj.getAttribute("atimestamp");
                    assertEquals(String.class.getName(),attr7.getClass().getName());
                    assertEquals("2015-02-16T08:35:45.000",attr7);
                }
                featureCollectionIter.close();
                dataStore.dispose();
            }
        } finally {
            TestUtilSqlPg.closeCon(con);
        }
    }
}
