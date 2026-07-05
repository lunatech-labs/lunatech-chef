
import { externalAttendanceEditedFailed } from "./ExternalAttendanceSlice";
import { fetchSchedulesExternalAttendance } from "../schedules/SchedulesActionCreators";
import { axiosInstance } from "../Axios";

export const editExternalAttendance = (externalAttendance) => (dispatch) => {
    const externalAttendanceToEdit = {
        attendancesCount: externalAttendance.attendancesCount,
    };

    axiosInstance
        .put("/externalAttendances/" + externalAttendance.uuid, externalAttendanceToEdit)
        .then(function () {
            dispatch(fetchSchedulesExternalAttendance());
        })
        .catch(function (error) {
            console.log("Failed adding Schedule external attendance: " + error);
            dispatch(externalAttendanceEditedFailed(error.message));
        });
};

