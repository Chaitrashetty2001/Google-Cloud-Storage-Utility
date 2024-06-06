package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GcsFileMetadata {
	private String url;
    private String metadata;
    private byte[] content;
	public GcsFileMetadata(String url, String metadata, byte[] content) {
		super();
		this.url = url;
		this.metadata = metadata;
		this.content = content;
	}

}
