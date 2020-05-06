import React from "react";
import { Loading } from "./Loading";

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
      return data.map((item) => (
        <div className="col-12 col-md-5 m-1" key={item.uuid}>
          <div>Name: {item.name}</div>
        </div>
      ));
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
