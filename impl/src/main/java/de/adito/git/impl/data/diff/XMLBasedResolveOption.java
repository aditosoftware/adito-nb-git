package de.adito.git.impl.data.diff;

import lombok.NonNull;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author m.kaspera, 26.10.2022
 */
abstract class XMLBasedResolveOption implements ResolveOption
{
  private final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

  NodeList parseConflictText(@NonNull ByteArrayInputStream pInputStream) throws IOException, SAXException, ParserConfigurationException
  {
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document document = db.parse(pInputStream);
    return document.getChildNodes();
  }

}
