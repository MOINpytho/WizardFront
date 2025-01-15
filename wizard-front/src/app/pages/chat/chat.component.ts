import { Component } from '@angular/core';
import { SharedModule } from '../../utils/shared.component';
import { ApiService } from '../../services/api.service';
import { Message } from '../../models/message';

@Component({
  selector: 'app-chat',
  imports:  [SharedModule],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent {
  constructor(private apiService:ApiService) { }

  inputText: String = "";
  messages: Message[] = [];
  loading: boolean = false;

  askButtonClicked(){
    if(this.inputText.trim() == ""){
      return;
    }
    //true if the message is from the user
    this.messages.push(new Message(this.inputText, true));
    this.loading = true;

    this.apiService.getResponse("inputText").
    subscribe({
      next: (response) => {
        console.log(response);
        //false if the message is from the bot
        this.messages.push(new Message(response, false));
        this.inputText = "";
        this.loading = false;
      },
      error: (error) => {
        console.log(error);
      }      
  })
  }
}
