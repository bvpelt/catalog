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

    @Column(name = "TITEL")
    private String titel;

    @Column(name = "METADATA")
    private String metadata;

    @Column(name = "WEBPAGINA")
    private String webpagina;

    @Column(name = "RESOURCE")
    private String resource;

    @Column(name = "BEGINDATUM")
    private String begindatumGeldigheid;

    @Column(name = "EINDDATUM")
    private String einddatumGeldigheid;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "EIGENAAR")
    private String eigenaar;

    @Column(name = "URI")
    private String uri;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BronDTO)) return false;
        BronDTO bronDTO = (BronDTO) o;
        return Objects.equals(titel, bronDTO.titel) && Objects.equals(metadata, bronDTO.metadata) && Objects.equals(webpagina, bronDTO.webpagina) && Objects.equals(resource, bronDTO.resource) && Objects.equals(begindatumGeldigheid, bronDTO.begindatumGeldigheid) && Objects.equals(einddatumGeldigheid, bronDTO.einddatumGeldigheid) && Objects.equals(type, bronDTO.type) && Objects.equals(eigenaar, bronDTO.eigenaar) && Objects.equals(uri, bronDTO.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(titel, metadata, webpagina, resource, begindatumGeldigheid, einddatumGeldigheid, type, eigenaar, uri);
    }
}


