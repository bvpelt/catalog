package com.bsoft.catalogus.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity(name = "BronDTO")
@Table(name = "BRON")
@Data
public class BronDTO implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "URI")
    private String uri;

    @Column(name = "TITEL")
    private String titel;

    @Column(name = "WEBPAGINA")
    private String webpagina;

    @Column(name = "RESOURCE")
    private String resource;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "BEGINDATUM")
    private String begindatumGeldigheid;

    @Column(name = "EINDDATUM")
    private String einddatumGeldigheid;

    @Column(name = "EIGENAAR")
    private String eigenaar;

    @Column(name = "METADATA")
    private String metadata;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BronDTO)) return false;
        BronDTO bronDTO = (BronDTO) o;
        return uri.equals(bronDTO.uri) && Objects.equals(titel, bronDTO.titel) && Objects.equals(webpagina, bronDTO.webpagina) && Objects.equals(resource, bronDTO.resource) && Objects.equals(type, bronDTO.type) && Objects.equals(begindatumGeldigheid, bronDTO.begindatumGeldigheid) && Objects.equals(einddatumGeldigheid, bronDTO.einddatumGeldigheid) && Objects.equals(eigenaar, bronDTO.eigenaar) && Objects.equals(metadata, bronDTO.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, titel, webpagina, resource, type, begindatumGeldigheid, einddatumGeldigheid, eigenaar, metadata);
    }
}


