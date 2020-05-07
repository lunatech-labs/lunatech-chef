import React from "react";
import { Loading } from "./Loading";
import { ConvertJsonToTable } from "rn-json-to-html-table";
import ReactHtmlParser from "react-html-parser";

const ListPresentation = (props) => {
  function RenderData({ isLoading, error, data }) {
    if (isLoading) {
      return (
        <div className="container">
          <div className="row">
            <Loading />
          </div>
        </div>
      );
    } else if (error) {
      return <h4>An error ocurred: {error}</h4>;
    } else {
      let htmlTable = ConvertJsonToTable(data, "generatedTable");
      return (
        <div className="col-12 col-md-5 m-1">{ReactHtmlParser(htmlTable)}</div>
      );
    }
  }

  return (
    <div>
      <RenderData
        isLoading={props.isLoading}
        error={props.error}
        data={props.data}
      />
    </div>
  );
};

export default ListPresentation;
