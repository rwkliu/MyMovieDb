import React from "react";
import {useUser} from "hook/User";
import styled from "styled-components";
import {useForm} from "react-hook-form";
import {register, registerUser} from "backend/idm";
import {useNavigate} from "react-router-dom";

const StyledDiv = styled.div`
  display: flex;
  flex-direction: column;
`

const StyledH1 = styled.h1`
`

const StyledInput = styled.input`
`

const StyledButton = styled.button`
`
const Register = () => {

    const {register, getValues, handleSubmit} = useForm();
    const navigate = useNavigate();
    const submitLogin = () => {
        const email = getValues("email");
        const password = getValues("password");

        const payLoad = {
            email: email,
            password: password
        }

        registerUser(payLoad)
            .then(response => {alert(JSON.stringify(response.data, null, 2));
                navigate('/login')})
            .catch(error => alert(JSON.stringify(error.response.data, null, 2)))
    }

    return (
        <StyledDiv>
            <h1>Register</h1>
            <label> Email: <input {...register("email")} type={"email"}/></label>
            <label> Password:<input {...register("password")} type={"password"}/></label>
            <button onClick={handleSubmit(submitLogin)}>Register</button>
        </StyledDiv>
    );
}

export default Register;
