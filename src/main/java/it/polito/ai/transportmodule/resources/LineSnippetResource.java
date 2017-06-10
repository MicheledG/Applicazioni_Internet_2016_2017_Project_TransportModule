package it.polito.ai.transportmodule.resources;

import org.springframework.hateoas.ResourceSupport;

public class LineSnippetResource extends ResourceSupport {
	private String line;
	
	public LineSnippetResource(String lineName){
		this.setLine(lineName);
	}
	public String getLine() {
		return line;
	}
	public void setLine(String line) {
		this.line = line;
	}
}
