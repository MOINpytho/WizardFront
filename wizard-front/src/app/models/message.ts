export class Message{
  text: String;
  isUser: boolean;
  constructor(text: String, isUser:boolean){
    this.text = text;
    this.isUser = isUser;
  }
}