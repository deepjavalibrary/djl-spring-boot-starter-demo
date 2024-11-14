# DJL Spring Boot Demo

This repository contains example code demonstrating how to use the [Deep Java Library](https://github.com/awslabs/djl) (DJL) with Spring Boot and the DJL Spring Boot Starter.
It covers MXNet-based object detection inference with platform specific DJL libraries that can be consumed using DJL Spring Boot Starter dependencies.

## Building the backend
Examples use Gradle as the build tool. It is a multi-project build.
Before building, ensure that the DJL Spring Boot starter BOM is in your local maven repository. For more information, see the [DJL Spring Boot Starter repo](https://github.com/awslabs/djl-spring-boot-starter). You need to check out this repository and run `./mvnw install`.

To build the DJL Spring Boot microservice, run the following command:

    ./gradlew :djl-spring-boot-app:bootJar

This command detects the operating system on the system where the build is running and uses it for platform dependency resolution.

Platform specific builds (for CI): 

    ./gradlew :djl-spring-boot-app:bootJar -P osclassifier=linux-x86_64
    ./gradlew :djl-spring-boot-app:bootJar -P osclassifier=osx-x86_64
    ./gradlew :djl-spring-boot-app:bootJar -P osclassifier=win-x86_64
  
The produced artifacts will have the classifier in the name of the spring boot uber jar, e.g.

    djl-spring-boot-app/build/libs/djl-spring-boot-app-0.0.1-SNAPSHOT-linux-x86_64.jar
    djl-spring-boot-app/build/libs/djl-spring-boot-app-0.0.1-SNAPSHOT-osx-x86_64.jar


 ## Running the backend
 
Running the app requires the following:
  The S3 bucket (`djl-demo` by default) must be specified in `djl-spring-boot-app/src/main/resources/application.properties`.
  The bucket is expected to have two prefixes: `inbox`, where input is located, and `outbox`, where results are placed.
  `AWS_ACCESS_KEY` and `AWS_SECRET_KEY` set as environment variables for region `us-east-1`. 
  The IAM user must have read permissions for the `inbox` and write permissions for the `outbox`.
  
  Run the following command to start the backend based on the created JAR file, sample command for macOS:
      
    java -jar djl-spring-boot-app/build/libs/djl-spring-boot-app-0.0.1-SNAPSHOT-osx-x86_64.jar
  
  Alternatively you can use Gradle for execution, sample command for Windows:   
  
    gradlew :djl-spring-boot-app:bootRun -P osclassifier=win-x86_64
  
## Deploying to PCF

Make sure your manifest.yml file contains valid S3 access credentials. You can specify your own bucket in [application.properties](djl-spring-boot-app/src/main/resources/application.properties) and [web application.properties](djl-spring-boot-web/src/main/resources/application.properties). 

Use the following command to deploy:

    cf login
    cf push -f manifest.yml -p build/libs/djl-spring-boot-app-0.0.1-SNAPSHOT-linux-x86_64.jar

Check the logs:

    cd logs djl-demo --recent

## Deploying to EKS (Amazon Elastic Kubernetes Service)

The instructions in this section may be generalized for any vanilla Kubernetes cluster, however they contain EKS 
specific features, such as IAM Roles integration for Kubernetes Service Accounts (IRSA).

Deployment to EKS is implemented in such a way that credentials to access remote resources such as S3 buckets from 
the pod are not stored anywhere on the cluster, instead we will provision a role with the right IAM policy and use 
it instead.  

We will use [`eksctl` tool](https://eksctl.io/introduction/#installation) to provision an EKS cluster for 
demonstration purposes.
You also need to install [kubectl](https://docs.aws.amazon.com/eks/latest/userguide/install-kubectl.html) – A
command line tool for working with Kubernetes clusters.

Before you start, make sure you have AWS CLI installed and proper credentials to access your AWS account.
Steps 2, 4 and 5 are only needed if you would like to protect access to your S3 bucket properly (it is NOT a good 
idea to open up an bucket with read/write access to the world). 

1. Create EKS Cluster
    ```bash
    eksctl create cluster --name=djl-demo --nodes=2
    ```

2. Create an OIDC Identity Provider (for IRSA)
    ```
    eksctl utils associate-iam-oidc-provider --cluster djl-demo --approve
    ```
    
    More info: [EKS Workshop](https://www.eksworkshop.com/beginner/110_irsa/oidc-provider/)


3. Create `djl-demo-<youraccount>` S3 bucket.  We will use two prefixes: `inbox` for incoming images and `outbox` 
   for outgoing processed images with detected objects. 
   The bucket name must be unique so in this example we add a suffix for your AWS account number which should 
   replace the placeholder <YOUR_AWS_ACCOUNT>.
       
    ```bash
    aws s3 mb s3://djl-demo-<YOUR_AWS_ACCOUNT>
    ```
    
    Note: this will create a bucket that will require proper authorization. Objects in the bucket will not be accessible 
    publicly.
   

4. Create IAM Policy to access the S3 bucket that you created (will be used for pods deployed in the demo):

    - Create a copy of the `docs/AccesDjlBucketPolicyTemplate.json` as `AccessDjlBucketPolicy.json` **replacing 
    <YOUR_BUCKET_NAME> with your actual bucket name**. 
    
    ```bash
    cp docs/AccessDjlBucketPolicyTemplate.json AccessDjlBucketPolicy.json
    # edit your AccessDjlBucketPolicy.json file
    aws iam create-policy --policy-name AccessDjlBucket --policy-document file://AccessDjlBucketPolicy.json
    ```
    
    - You can get the ARN on of the created policy with the following command:
    
    ```bash
     aws iam list-policies --query 'Policies[?PolicyName==`AccessDjlBucket`].Arn'
    ```
   
5. Create a service account for the backend service: 
   
   **Note:** Adjust the namespace if you plan to deploy to a namespace different from default.
   
    ```bash
    eksctl create iamserviceaccount \
        --name djl-backend-account \
        --namespace default \
        --cluster djl-demo \
        --attach-policy-arn <ARN_OF_POLICY_OBTAINED_IN_STEP_4> \
        --approve \
        --override-existing-serviceaccounts
    ```

6. In this example we will use Amazon ECR private repository to push images. The repository will need to be created 
   upfront. 

    ```bash
    aws ecr create-repository --repository-name djl-spring-boot-app
    ```

7. Build and push the container image for the API app to an accessible container (Docker) registry.
   
    - You must be properly authenticated to push images to Amazon ECR. For more info see this
      [doc](https://docs.aws.amazon.com/AmazonECR/latest/userguide/docker-push-ecr-image.html).
      
   ```bash
   aws ecr get-login-password --region <YOUR_REGION> | docker login --username AWS --password-stdin <YOUR_AWS_ACCOUNT>.dkr.ecr.<YOUR_REGION>.amazonaws.com
   ```
   
    - Assuming you forked this repository, modify the jib section of the `djl-spring-boot-app/build.gradle.kts` to 
    reflect your settings (replace the placeholders):

    ```kotlin
    jib {
        from.image = "adoptopenjdk/openjdk13:debian"
        to.image = "<YOUR_AWS_ACCOUNT>.dkr.ecr.<YOUR_REGION>.amazonaws.com/djl-spring-boot-app"
        to.tags = versionTags
    } 
    ```

    - From the root directory:

    ```bash
    ./gradlew djl-spring-boot-app:bootjar
    ./gradlew djl-spring-boot-app:jib
    ```

    The above will push the image to the Amazon ECR container registry and output the image/tag pair that you will need 
    for the subsequent steps.


8. Deploy the application:
    
    - Modify the provided `docs/deployment-template.yaml` and specify your image:tag produced in step 7 

    - Run the command below to deploy the application and create a load balancer to access it over HTTP (don't use this 
   approach in production without TLS in place):
   
    ```bash
    # assuming you saved the modified deployment-template.yaml in the current directory as deployment.yaml
    kubectl apply -f deployment.yaml
    ``` 
   
    You can set the `-n YOUR_NAMESPACE` flag on the command if you created the service account in a different namespace.

    **Note:** this deployment is leveraging the service account `djl-backend-account` created in the previous steps. If you 
    don't need a service account (e.g. in case you modified the app to read from local storage), then remove the  
   service account association in the template (`serviceAccountName: djl-backend-account`).


9. Test your api:
   
   - Get the load balancer public DNS name by listing the created service
   
    ```bash
    # ensure the pod is in running state
    kubectl get po 
    # get the loadbalancer URL
    kubectl get svc djl-app
    ```
 
   - Upload a file to your S3 bucket `inbox` folder
   
    ```bash
        curl -O https://resources.djl.ai/images/kitten.jpg
        aws s3 cp kitten.jpg s3://<YOUR_BUCKET_NAME>/inbox/kitten.jpg
    ```

    - Test with curl
    ```bash
        curl -v "http://<YOUR_LOAD_BALANCER_DNS>/inference?file=kitten.jpg&generateOutputImage=true"
    ```

    - Get the output file from s3 `outbox` folder (**at present .png extension is always appended**)
    
    ```bash
        aws s3 ls s3://<YOUR_BUCKET_NAME>/outbox/
        aws s3 cp s3://<YOUR_BUCKET_NAME>/outbox/kitten.jpg.png
    ```

This completes the EKS deployment portion of the application.

The EKS deployment of the API can be modified to scale up and down based on demand and spin up pods as needed based 
on HPA. The recommended approach to deploy any workloads on EKS is to use GipOps approach such as [FluxCD](https://fluxcd.io/) or [ArgoCD](https://argoproj.github.io/argo-cd/).

## Frontend Web Application
[Web Application](djl-spring-boot-web/README.md)

## License
This project is licensed under the Apache-2.0 License.


