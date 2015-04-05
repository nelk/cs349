package quickmotion.shared;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 *
 */
public class DocumentManager {
    private Document document;
    private DocumentBuilder builder;
    private Transformer transformer;

    private static final String ROOT = "quickmotion";

    public static enum ElementNames {
        lines, animations, drawn_line, animation_id, line, segs, figure_id, animation, colour
    }


    public DocumentManager() {
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "YES");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            clear();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        document = builder.newDocument();
        document.appendChild(document.createElement(ROOT));
    }

    private Element cast(Node n) {
        if (n.getNodeType() == Node.ELEMENT_NODE) {
            return (Element) n;
        }
        return null;
    }

    public boolean load(File f, List<DrawnLine> drawnLines, List<Animation> animations) {
        try {
            document = builder.parse(f);

            HashMap<Long, Animation> animationMap = new HashMap<Long, Animation>();
            HashMap<Long, Figure> figureMap = new HashMap<Long, Figure>();

            NodeList animationNodeList = document.getElementsByTagName(ElementNames.animation.name());
            for (int i = 0; i < animationNodeList.getLength(); i++) {
                NodeList animationChildren = animationNodeList.item(i).getChildNodes();
                long id = 0;
                String segs = "";
                for (int j = 0; j < animationChildren.getLength(); j++) {
                    if (animationChildren.item(j) instanceof Element) {
                    Element elem = cast(animationChildren.item(j));
                    if (elem == null) continue;
                    if (elem.getTagName().equals(ElementNames.animation_id.name())) {
                        id = Long.parseLong(elem.getTextContent());
                    } else if (elem.getTagName().equals(ElementNames.segs.name())) {
                        segs = elem.getTextContent();
                    }
                    }
                }
                Animation a = new Animation();
                a.deserialize(segs);
                a.setId(id);
                animationMap.put(id, a);
                animations.add(a);
            }

            NodeList lineNodeList = document.getElementsByTagName(ElementNames.drawn_line.name());
            for (int i = 0; i < lineNodeList.getLength(); i++) {
                NodeList lineChildren = lineNodeList.item(i).getChildNodes();
                DrawnLine l = new DrawnLine();
                for (int j = 0; j < lineChildren.getLength(); j++) {
                    Element elem = cast(lineChildren.item(j));
                    if (elem == null) continue;
                    if (elem.getTagName().equals(ElementNames.line.name())) {
                        l.deserialize(elem.getTextContent());
                    } else if (elem.getTagName().equals(ElementNames.colour.name())) {
                        l.setColour(Integer.parseInt(elem.getTextContent()));
                    } else if (elem.getTagName().equals(ElementNames.figure_id.name())) {
                        long figureId = Long.parseLong(elem.getTextContent());
                        Figure figure;
                        if (figureMap.containsKey(figureId)) {
                            figure = figureMap.get(figureId);
                        } else {
                            figure = new Figure();
                            figure.setId(figureId);
                            figureMap.put(figureId, figure);
                        }
                        l.setFigure(figure);
                        figure.addLine(l);

                    } else if (elem.getTagName().equals(ElementNames.animation_id.name())) {
                        long animationId = Long.parseLong(elem.getTextContent());
                        Animation a = animationMap.get(animationId);
                        a.animateLine(l);
                    }
                }
                drawnLines.add(l);
            }

            clear();
            return true;
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException ignored) {
        }
        return false;
    }

    /**
     * Will save the lines, animations, and figures all from list of lines (irrelevant data won't be saved)
     * @param f the output file
     * @param lines collection of DrawnLines
     *
     * layout:
     *              <quickmotion>
     *                  <lines>
     *                      <drawn_line>
     *                          <line>1,2,3,4</line>
     *                          <colour>sRGB integer</colour>
     *                          <figure_id>0</figure_id>
     *                          <animation_id>2</animation_id>
     *                          <animation_id>3</animation_id>
     *                          ...
     *                      </drawn_line>
     *                      ...
     *                  </lines>
     *                  <animations>
     *                      <animation>
     *                          <animation_id>2</animation_id>
     *                          <segs>rotationRefPoint,startTime,type,0.00123,1,2,3,4,5,6...</segs> // time,a,b,c,d,e,f (a-f is a matrix) for each segment, all segments comma delimited also
     *                      </animation>
     *                      ...
     *                  </animations>
     *              </quickmotion>
     */
    public void save(File f, Collection<DrawnLine> lines) {
        HashSet<Animation> animations = new HashSet<Animation>();
        Element rootElement = cast(document.getFirstChild());
        if (rootElement == null) return;
        Element linesElement = document.createElement(ElementNames.lines.name());
        Element animationsElement = document.createElement(ElementNames.animations.name());
        rootElement.appendChild(linesElement);
        rootElement.appendChild(animationsElement);

        for (DrawnLine l : lines) {
            Element lElement = document.createElement(ElementNames.drawn_line.name());
            linesElement.appendChild(lElement);

            Element vectorElement = document.createElement(ElementNames.line.name());
            vectorElement.setTextContent(l.serialize());
            lElement.appendChild(vectorElement);

            Element colourElement = document.createElement(ElementNames.colour.name());
            colourElement.setTextContent(String.valueOf(l.getColour()));
            lElement.appendChild(colourElement);

            String figureId = String.valueOf(l.getFigure().getId());
            Element figureElement = document.createElement(ElementNames.figure_id.name());
            figureElement.setTextContent(figureId);
            lElement.appendChild(figureElement);



            for (Animation a : l.getAnimations()) {
                animations.add(a);
                Element animIdElement = document.createElement(ElementNames.animation_id.name());
                animIdElement.setTextContent(String.valueOf(a.getId()));
                lElement.appendChild(animIdElement);
            }
        }

        for (Animation a : animations) {
            Element aElement = document.createElement(ElementNames.animation.name());
            animationsElement.appendChild(aElement);

            Element animIdElement = document.createElement(ElementNames.animation_id.name());
            animIdElement.setTextContent(String.valueOf(a.getId()));
            aElement.appendChild(animIdElement);

            Element matElement = document.createElement(ElementNames.segs.name());
            matElement.setTextContent(a.serialize());
            aElement.appendChild(matElement);
        }


        try {
            transformer.transform(new DOMSource(document), new StreamResult(f));
            clear();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

}

